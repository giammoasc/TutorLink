package it.uniroma2.tutorlink.dao.filesystem;

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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileSystemLessonDao extends AbstractCsvDao<Lesson> implements LessonDao {
    private static final String FILE_NAME = "lessons.csv";

    private final UserDao userDao;
    private final AvailabilityDao availabilityDao;
    private final FeedbackDao feedbackDao;

    public FileSystemLessonDao(Path root, UserDao userDao, AvailabilityDao availabilityDao,
                               FeedbackDao feedbackDao) throws PersistenceException {
        super(root, FILE_NAME);
        this.userDao = userDao;
        this.availabilityDao = availabilityDao;
        this.feedbackDao = feedbackDao;
    }

    @Override
    protected String[] toRecord(Lesson lesson) {
        return new String[]{
                Long.toString(lesson.id()),
                lesson.student().email(),
                lesson.tutor().email(),
                lesson.subject().name(),
                lesson.slot().start().toString(),
                Integer.toString(lesson.slot().minutes()),
                lesson.price().amount().toPlainString(),
                lesson.stateName(),
                lesson.meetingLink().orElse(""),
                lesson.origin().map(availability -> Long.toString(availability.id())).orElse("")};
    }

    @Override
    protected Lesson fromRecord(String[] record) throws PersistenceException {
        if (record.length < 9) {
            throw new PersistenceException("malformed record in " + file());
        }
        long id = Long.parseLong(record[0]);
        IdGenerator.observe(id);
        Student student = userDao.findByEmail(record[1])
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .orElseThrow(() -> new PersistenceException("the student " + record[1] + " does not exist any more"));
        Tutor tutor = userDao.findByEmail(record[2])
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .orElseThrow(() -> new PersistenceException("the tutor " + record[2] + " does not exist any more"));
        TimeSlot slot = new TimeSlot(LocalDateTime.parse(record[4]), Integer.parseInt(record[5]));
        Availability origin = null;
        if (record.length > 9 && !record[9].isBlank()) {
            origin = availabilityDao.findById(Long.parseLong(record[9])).orElse(null);
        }
        String link = record[8].isBlank() ? null : record[8];
        Lesson lesson = new Lesson(id, student, tutor, Subject.valueOf(record[3]), slot,
                Money.of(new BigDecimal(record[6])), origin, LessonStates.of(record[7]), link);
        feedbackDao.findByLesson(lesson).ifPresent(lesson::restoreFeedback);
        return lesson;
    }

    @Override
    protected String keyOf(String[] record) {
        return record[0];
    }

    @Override
    public void save(Lesson lesson) throws PersistenceException {
        upsert(lesson);
    }

    @Override
    public void update(Lesson lesson) throws PersistenceException {
        upsert(lesson);
    }

    @Override
    public Optional<Lesson> findById(long id) throws PersistenceException {
        String key = Long.toString(id);
        for (String[] record : readRecords()) {
            if (keyOf(record).equals(key)) {
                return Optional.of(fromRecord(record));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Lesson> findByStudent(Student student) throws PersistenceException {
        return findByColumn(1, student.email());
    }

    @Override
    public List<Lesson> findByTutor(Tutor tutor) throws PersistenceException {
        return findByColumn(2, tutor.email());
    }

    private List<Lesson> findByColumn(int column, String email) throws PersistenceException {
        List<Lesson> lessons = new ArrayList<>();
        for (String[] record : readRecords()) {
            if (record[column].equalsIgnoreCase(email)) {
                lessons.add(fromRecord(record));
            }
        }
        return lessons;
    }
}
