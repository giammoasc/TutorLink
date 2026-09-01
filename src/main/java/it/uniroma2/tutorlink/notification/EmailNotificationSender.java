package it.uniroma2.tutorlink.notification;

import it.uniroma2.tutorlink.model.Notification;
import java.util.logging.Level;
import java.util.logging.Logger;

// Canale email simulato: scrive sul log.
public class EmailNotificationSender implements NotificationSender {
    private static final Logger LOGGER = Logger.getLogger(EmailNotificationSender.class.getName());

    @Override
    public void send(Notification notification) {
        LOGGER.log(Level.INFO, "e-mail to {0}: {1}",
                new Object[]{notification.recipient().email(), notification.message()});
    }

    @Override
    public String channel() {
        return "email";
    }
}
