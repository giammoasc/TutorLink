package it.uniroma2.tutorlink.support;

import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.notification.NotificationSender;
import java.util.ArrayList;
import java.util.List;

// Finto canale di notifica: le registra invece di mandarle.
public class RecordingNotificationSender implements NotificationSender {
    private final List<Notification> sent = new ArrayList<>();

    @Override
    public void send(Notification notification) {
        sent.add(notification);
    }

    @Override
    public String channel() {
        return "recording";
    }

    public List<Notification> sent() {
        return List.copyOf(sent);
    }

    public int count() {
        return sent.size();
    }
}
