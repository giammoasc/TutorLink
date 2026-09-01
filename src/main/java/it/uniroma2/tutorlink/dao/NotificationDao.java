package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.User;
import java.util.List;

public interface NotificationDao {
    void save(Notification notification) throws PersistenceException;

    void update(Notification notification) throws PersistenceException;

    List<Notification> findByRecipient(User recipient) throws PersistenceException;
}
