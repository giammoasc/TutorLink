package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.state.LessonStates;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLessonDao implements LessonDao {
    private static final String COLUMNS =
            "id, student_email, tutor_email, subject, start_at, minutes, price, state, meeting_link, availability_id";
    private static final String UPSERT =
            "INSERT INTO lesson (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE state = VALUES(state), meeting_link = VALUES(meeting_link), "
                    + "price = VALUES(price)";
    private static final String SELECT_BY_ID = "SELECT " + COLUMNS + " FROM lesson WHERE id = ?";
    private static final String SELECT_BY_STUDENT =
            "SELECT " + COLUMNS + " FROM lesson WHERE student_email = ? ORDER BY start_at";
    private static final String SELECT_BY_TUTOR =
            "SELECT " + COLUMNS + " FROM lesson WHERE tutor_email = ? ORDER BY start_at";

    private final ConnectionFactory connections;
    private final UserDao userDao;
    private final AvailabilityDao availabilityDao;
    private final FeedbackDao feedbackDao;

    public JdbcLessonDao(ConnectionFactory connections, UserDao userDao,
                         AvailabilityDao availabilityDao, FeedbackDao feedbackDao) {
        this.connections = connections;
        this.userDao = userDao;
        this.availabilityDao = availabilityDao;
        this.feedbackDao = feedbackDao;
    }

    @Override
    public void save(Lesson lesson) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setLong(1, lesson.id());
            statement.setString(2, lesson.student().email());
            statement.setString(3, lesson.tutor().email());
            statement.setString(4, lesson.subject().name());
            statement.setTimestamp(5, Timestamp.valueOf(lesson.slot().start()));
            statement.setInt(6, lesson.slot().minutes());
            statement.setBigDecimal(7, lesson.price().amount());
            statement.setString(8, lesson.stateName());
            statement.setString(9, lesson.meetingLink().orElse(null));
            if (lesson.origin().isPresent()) {
                statement.setLong(10, lesson.origin().orElseThrow().id());
            } else {
                statement.setNull(10, java.sql.Types.BIGINT);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("the lesson " + lesson.id() + " cannot be stored", e);
        }
    }

    @Override
    public void update(Lesson lesson) throws PersistenceException {
        save(lesson);
    }

    @Override
    public Optional<Lesson> findById(long id) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("the lesson " + id + " cannot be read", e);
        }
    }

    @Override
    public List<Lesson> findByStudent(Student student) throws PersistenceException {
        return findByEmail(SELECT_BY_STUDENT, student.email());
    }

    @Override
    public List<Lesson> findByTutor(Tutor tutor) throws PersistenceException {
        return findByEmail(SELECT_BY_TUTOR, tutor.email());
    }

    private List<Lesson> findByEmail(String query, String email) throws PersistenceException {
        List<Lesson> lessons = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lessons.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("the lessons of " + email + " cannot be read", e);
        }
        return lessons;
    }

    private Lesson mapRow(ResultSet resultSet) throws SQLException, PersistenceException {
        long id = resultSet.getLong("id");
        IdGenerator.observe(id);
        String studentEmail = resultSet.getString("student_email");
        String tutorEmail = resultSet.getString("tutor_email");
        Student student = userDao.findByEmail(studentEmail)
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .orElseThrow(() -> new PersistenceException("the student " + studentEmail + " does not exist any more"));
        Tutor tutor = userDao.findByEmail(tutorEmail)
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .orElseThrow(() -> new PersistenceException("the tutor " + tutorEmail + " does not exist any more"));
        TimeSlot slot = new TimeSlot(resultSet.getTimestamp("start_at").toLocalDateTime(),
                resultSet.getInt("minutes"));
        long availabilityId = resultSet.getLong("availability_id");
        Availability origin = resultSet.wasNull() ? null : availabilityDao.findById(availabilityId).orElse(null);
        Lesson lesson = new Lesson(id, student, tutor, Subject.valueOf(resultSet.getString("subject")), slot,
                Money.of(resultSet.getBigDecimal("price")), origin,
                LessonStates.of(resultSet.getString("state")), resultSet.getString("meeting_link"));
        feedbackDao.findByLesson(lesson).ifPresent(lesson::restoreFeedback);
        return lesson;
    }
}
