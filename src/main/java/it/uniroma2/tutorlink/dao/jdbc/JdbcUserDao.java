package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import it.uniroma2.tutorlink.model.UserRole;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private static final String SELECT_BY_EMAIL =
            "SELECT email, full_name, password_hash, role, hourly_rate, subjects FROM app_user WHERE email = ?";
    private static final String SELECT_TUTORS_BY_SUBJECT =
            "SELECT email, full_name, password_hash, role, hourly_rate, subjects FROM app_user "
                    + "WHERE role = 'TUTOR' AND FIND_IN_SET(?, subjects) > 0";
    private static final String SELECT_STUDENTS =
            "SELECT email, full_name, password_hash, role, hourly_rate, subjects FROM app_user WHERE role = 'STUDENT'";
    private static final String UPSERT =
            "INSERT INTO app_user (email, full_name, password_hash, role, hourly_rate, subjects) "
                    + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                    + "full_name = VALUES(full_name), password_hash = VALUES(password_hash), "
                    + "hourly_rate = VALUES(hourly_rate), subjects = VALUES(subjects)";

    private final ConnectionFactory connections;

    public JdbcUserDao(ConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public Optional<User> findByEmail(String email) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EMAIL)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("the account " + email + " cannot be read", e);
        }
    }

    @Override
    public boolean exists(String email) throws PersistenceException {
        return findByEmail(email).isPresent();
    }

    @Override
    public void save(User user) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setString(1, user.email());
            statement.setString(2, user.fullName());
            statement.setString(3, user.passwordDigest());
            statement.setString(4, user.role().name());
            if (user instanceof Tutor) {
                Tutor tutor = (Tutor) user;
                statement.setBigDecimal(5, tutor.hourlyRate().amount());
                statement.setString(6, joinSubjects(tutor));
            } else {
                statement.setNull(5, java.sql.Types.DECIMAL);
                statement.setNull(6, java.sql.Types.VARCHAR);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("the account " + user.email() + " cannot be stored", e);
        }
    }

    @Override
    public List<Tutor> findTutorsBySubject(Subject subject) throws PersistenceException {
        List<Tutor> tutors = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_TUTORS_BY_SUBJECT)) {
            statement.setString(1, subject.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tutors.add((Tutor) mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("the tutors of " + subject + " cannot be read", e);
        }
        return tutors;
    }

    @Override
    public List<Student> findAllStudents() throws PersistenceException {
        List<Student> students = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_STUDENTS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                students.add((Student) mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new PersistenceException("the students cannot be read", e);
        }
        return students;
    }

    private static String joinSubjects(Tutor tutor) {
        return tutor.subjects().stream().map(Enum::name).reduce((a, b) -> a + "," + b).orElse("");
    }

    private static User mapRow(ResultSet resultSet) throws SQLException {
        String email = resultSet.getString("email");
        String fullName = resultSet.getString("full_name");
        String digest = resultSet.getString("password_hash");
        UserRole role = UserRole.valueOf(resultSet.getString("role"));
        if (role == UserRole.STUDENT) {
            return new Student(email, fullName, digest);
        }
        BigDecimal rate = resultSet.getBigDecimal("hourly_rate");
        Tutor tutor = new Tutor(email, fullName, digest, Money.of(rate == null ? BigDecimal.ZERO : rate));
        String subjects = resultSet.getString("subjects");
        if (subjects != null && !subjects.isBlank()) {
            for (String subject : subjects.split(",")) {
                tutor.teach(Subject.valueOf(subject.trim()));
            }
        }
        return tutor;
    }
}
