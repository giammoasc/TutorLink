package it.uniroma2.tutorlink.notification;

import it.uniroma2.tutorlink.model.Notification;

public interface NotificationSender {
    void send(Notification notification);

    String channel();
}
