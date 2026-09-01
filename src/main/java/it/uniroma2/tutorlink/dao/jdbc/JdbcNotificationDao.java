package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.User;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcNotificationDao implements NotificationDao {
    private static final String UPSERT =
            "INSERT INTO notification (id, recipient_email, message, created_at, seen) VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE seen = VALUES(seen)";
    private static final String SELECT_BY_RECIPIENT =
            "SELECT id, recipient_email, message, created_at, seen FROM notification "
                    + "WHERE recipient_email = ? ORDER BY created_at DESC";

    private final ConnectionFactory connections;
    private final UserDao userDao;

    public JdbcNotificationDao(ConnectionFactory connections, UserDao userDao) {
        this.connections = connections;
        this.userDao = userDao;
    }

    @Override
    public void save(Notification notification) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setLong(1, notification.id());
            statement.setString(2, notification.recipient().email());
            statement.setString(3, notification.message());
            statement.setTimestamp(4, Timestamp.valueOf(notification.createdAt()));
            statement.setBoolean(5, notification.isSeen());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("the notification " + notification.id() + " cannot be stored", e);
        }
    }

    @Override
    public void update(Notification notification) throws PersistenceException {
        save(notification);
    }

    @Override
    public List<Notification> findByRecipient(User recipient) throws PersistenceException {
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_RECIPIENT)) {
            statement.setString(1, recipient.email());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    IdGenerator.observe(id);
                    notifications.add(new Notification(id, recipient, resultSet.getString("message"),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getBoolean("seen")));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("the notifications of " + recipient.email() + " cannot be read", e);
        }
        return notifications;
    }
}
