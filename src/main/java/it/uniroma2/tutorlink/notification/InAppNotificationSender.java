package it.uniroma2.tutorlink.notification;

import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Notification;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InAppNotificationSender implements NotificationSender {
    private static final Logger LOGGER = Logger.getLogger(InAppNotificationSender.class.getName());

    private final NotificationDao notificationDao;

    public InAppNotificationSender(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public void send(Notification notification) {
        try {
            notificationDao.save(notification);
        } catch (PersistenceException e) {
            // se la notifica non si salva non annullo l'operazione che l'ha generata
            LOGGER.log(Level.WARNING, e,
                    () -> "the notification for " + notification.recipient().email() + " could not be stored");
        }
    }

    @Override
    public String channel() {
        return "in-app";
    }
}
