package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class JdbcFeedbackDao implements FeedbackDao {
    private static final String UPSERT =
            "INSERT INTO feedback (lesson_id, score, comment, created_at) VALUES (?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE score = VALUES(score), comment = VALUES(comment)";
    private static final String SELECT_BY_LESSON =
            "SELECT score, comment, created_at FROM feedback WHERE lesson_id = ?";

    private final ConnectionFactory connections;

    public JdbcFeedbackDao(ConnectionFactory connections) {
        this.connections = connections;
    }

    @Override
    public void save(Feedback feedback) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setLong(1, feedback.lesson().id());
            statement.setInt(2, feedback.score());
            statement.setString(3, feedback.comment());
            statement.setTimestamp(4, Timestamp.valueOf(feedback.createdAt()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException(
                    "the feedback of the lesson " + feedback.lesson().id() + " cannot be stored", e);
        }
    }

    @Override
    public Optional<Feedback> findByLesson(Lesson lesson) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_LESSON)) {
            statement.setLong(1, lesson.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Feedback(lesson, resultSet.getInt("score"),
                        resultSet.getString("comment"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()));
            }
        } catch (SQLException e) {
            throw new PersistenceException(
                    "the feedback of the lesson " + lesson.id() + " cannot be read", e);
        }
    }
}
