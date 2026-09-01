package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.User;
import java.util.Comparator;
import java.util.List;

public class InMemoryNotificationDao implements NotificationDao {
    private final InMemoryStore store;

    public InMemoryNotificationDao(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void save(Notification notification) {
        store.notifications().put(notification.id(), notification);
    }

    @Override
    public void update(Notification notification) {
        save(notification);
    }

    @Override
    public List<Notification> findByRecipient(User recipient) {
        return store.notifications().values().stream()
                .filter(notification -> notification.recipient().equals(recipient))
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .toList();
    }
}
