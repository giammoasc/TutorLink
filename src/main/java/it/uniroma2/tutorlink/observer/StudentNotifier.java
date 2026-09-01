package it.uniroma2.tutorlink.observer;

import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.notification.NotificationSender;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// Avvisa gli studenti che hanno gia' fatto lezione con quel tutor.
public class StudentNotifier implements AvailabilityObserver {
    private static final Logger LOGGER = Logger.getLogger(StudentNotifier.class.getName());
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LessonDao lessonDao;
    private final NotificationSender sender;

    public StudentNotifier(LessonDao lessonDao, NotificationSender sender) {
        this.lessonDao = lessonDao;
        this.sender = sender;
    }

    @Override
    public void onAvailabilityPublished(Availability availability) {
        String message = availability.tutor().fullName() + " has published a new slot on "
                + DATE_TIME.format(availability.slot().start());
        for (Student student : interestedStudents(availability)) {
            sender.send(new Notification(IdGenerator.next(), student, message, LocalDateTime.now()));
        }
    }

    private Set<Student> interestedStudents(Availability availability) {
        Set<Student> students = new LinkedHashSet<>();
        try {
            for (Lesson lesson : lessonDao.findByTutor(availability.tutor())) {
                students.add(lesson.student());
            }
        } catch (PersistenceException e) {
            // se non riesco a leggere le lezioni non mando niente, ma non blocco la pubblicazione
            LOGGER.log(Level.WARNING, e,
                    () -> "the audience of " + availability.tutor().email() + " could not be computed");
        }
        return students;
    }
}
