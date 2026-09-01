package it.uniroma2.tutorlink.notification;

import it.uniroma2.tutorlink.model.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Piu' canali usati come se fossero uno solo.
public class CompositeNotificationSender implements NotificationSender {
    private final List<NotificationSender> children = new ArrayList<>();

    public CompositeNotificationSender(NotificationSender... senders) {
        for (NotificationSender sender : senders) {
            add(sender);
        }
    }

    public final void add(NotificationSender sender) {
        if (sender != null) {
            children.add(sender);
        }
    }

    @Override
    public void send(Notification notification) {
        for (NotificationSender sender : children) {
            sender.send(notification);
        }
    }

    @Override
    public String channel() {
        return children.stream().map(NotificationSender::channel).collect(Collectors.joining("+"));
    }
}
