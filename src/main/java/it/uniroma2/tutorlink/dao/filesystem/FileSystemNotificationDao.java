package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.User;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileSystemNotificationDao extends AbstractCsvDao<Notification> implements NotificationDao {
    private static final String FILE_NAME = "notifications.csv";

    private final UserDao userDao;

    public FileSystemNotificationDao(Path root, UserDao userDao) throws PersistenceException {
        super(root, FILE_NAME);
        this.userDao = userDao;
    }

    @Override
    protected String[] toRecord(Notification notification) {
        return new String[]{
                Long.toString(notification.id()),
                notification.recipient().email(),
                notification.message(),
                notification.createdAt().toString(),
                Boolean.toString(notification.isSeen())};
    }

    @Override
    protected Notification fromRecord(String[] record) throws PersistenceException {
        if (record.length < 5) {
            throw new PersistenceException("malformed record in " + file());
        }
        long id = Long.parseLong(record[0]);
        IdGenerator.observe(id);
        User recipient = userDao.findByEmail(record[1])
                .orElseThrow(() -> new PersistenceException("the user " + record[1] + " does not exist any more"));
        return new Notification(id, recipient, record[2],
                LocalDateTime.parse(record[3]), Boolean.parseBoolean(record[4]));
    }

    @Override
    protected String keyOf(String[] record) {
        return record[0];
    }

    @Override
    public void save(Notification notification) throws PersistenceException {
        upsert(notification);
    }

    @Override
    public void update(Notification notification) throws PersistenceException {
        upsert(notification);
    }

    @Override
    public List<Notification> findByRecipient(User recipient) throws PersistenceException {
        List<Notification> notifications = new ArrayList<>();
        for (String[] record : readRecords()) {
            if (record[1].equalsIgnoreCase(recipient.email())) {
                notifications.add(fromRecord(record));
            }
        }
        notifications.sort(Comparator.comparing(Notification::createdAt).reversed());
        return notifications;
    }
}
