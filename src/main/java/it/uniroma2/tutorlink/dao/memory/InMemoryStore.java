package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.User;
import java.util.LinkedHashMap;
import java.util.Map;

// Le mappe che tengono i dati della modalita' demo.
public class InMemoryStore {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<Long, Availability> availabilities = new LinkedHashMap<>();
    private final Map<Long, Lesson> lessons = new LinkedHashMap<>();
    private final Map<Long, Material> materials = new LinkedHashMap<>();
    private final Map<Long, byte[]> contents = new LinkedHashMap<>();
    private final Map<Long, Feedback> feedbacks = new LinkedHashMap<>();
    private final Map<Long, Notification> notifications = new LinkedHashMap<>();

    public Map<String, User> users() {
        return users;
    }

    public Map<Long, Availability> availabilities() {
        return availabilities;
    }

    public Map<Long, Lesson> lessons() {
        return lessons;
    }

    public Map<Long, Material> materials() {
        return materials;
    }

    public Map<Long, byte[]> contents() {
        return contents;
    }

    public Map<Long, Feedback> feedbacks() {
        return feedbacks;
    }

    public Map<Long, Notification> notifications() {
        return notifications;
    }


}
