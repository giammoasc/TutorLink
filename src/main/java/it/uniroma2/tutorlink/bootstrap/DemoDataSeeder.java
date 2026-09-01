package it.uniroma2.tutorlink.bootstrap;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.OverlappingAvailabilityException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.state.LessonStates;
import it.uniroma2.tutorlink.util.IdGenerator;
import it.uniroma2.tutorlink.util.PasswordHasher;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

// Crea account, storico e slot di esempio quando non c'e' ancora niente.
public final class DemoDataSeeder {
    private static final Logger LOGGER = Logger.getLogger(DemoDataSeeder.class.getName());
    private static final String DEFAULT_PASSWORD = "tutorlink";
    private static final int LESSON_MINUTES = 60;

    private final DaoFactory daos;

    public DemoDataSeeder(DaoFactory daos) {
        this.daos = daos;
    }

    public void seedIfEmpty() {
        try {
            UserDao userDao = daos.createUserDao();
            if (userDao.exists("mario.rossi@students.uniroma2.eu")) {
                return;
            }
            seed(userDao);
        } catch (PersistenceException | OverlappingAvailabilityException e) {
            LOGGER.log(Level.WARNING, e, () -> "the demo data set could not be created");
        }
    }

    private void seed(UserDao userDao) throws PersistenceException, OverlappingAvailabilityException {
        Student student = new Student("mario.rossi@students.uniroma2.eu", "Mario Rossi", freshDigest());
        Tutor analyst = new Tutor("giulia.bianchi@uniroma2.eu", "Giulia Bianchi", freshDigest(), Money.of(28));
        analyst.teach(Subject.MATHEMATICS);
        analyst.teach(Subject.PHYSICS);
        Tutor developer = new Tutor("luca.verdi@uniroma2.eu", "Luca Verdi", freshDigest(), Money.of(35));
        developer.teach(Subject.COMPUTER_SCIENCE);
        developer.teach(Subject.MATHEMATICS);

        userDao.save(student);
        userDao.save(analyst);
        userDao.save(developer);

        seedHistory(student, analyst, developer);
        seedFutureSlots(analyst, developer);
    }

    private static String freshDigest() {
        char[] password = DEFAULT_PASSWORD.toCharArray();
        try {
            return PasswordHasher.hash(password);
        } finally {
            PasswordHasher.wipe(password);
        }
    }

    private void seedHistory(Student student, Tutor analyst, Tutor developer) throws PersistenceException {
        LocalDateTime base = LocalDateTime.now().minusDays(30).withHour(10).withMinute(0);
        recordCompletedLesson(student, analyst, Subject.MATHEMATICS, base, 5);
        recordCompletedLesson(student, analyst, Subject.MATHEMATICS, base.plusDays(7).withHour(16), 6);
        recordCompletedLesson(student, developer, Subject.COMPUTER_SCIENCE, base.plusDays(14).withHour(10), 9);
    }

    private void recordCompletedLesson(Student student, Tutor tutor, Subject subject,
                                       LocalDateTime start, int score) throws PersistenceException {
        LessonDao lessonDao = daos.createLessonDao();
        FeedbackDao feedbackDao = daos.createFeedbackDao();

        TimeSlot slot = new TimeSlot(start, LESSON_MINUTES);
        Lesson lesson = new Lesson(IdGenerator.next(), student, tutor, subject, slot,
                tutor.priceFor(slot), null, LessonStates.of("COMPLETED"), null);
        Feedback feedback = new Feedback(lesson, score, "keep working on the exercises",
                start.plusMinutes(LESSON_MINUTES));
        lesson.restoreFeedback(feedback);

        lessonDao.save(lesson);
        feedbackDao.save(feedback);
    }

    private void seedFutureSlots(Tutor analyst, Tutor developer)
            throws PersistenceException, OverlappingAvailabilityException {
        AvailabilityDao availabilityDao = daos.createAvailabilityDao();
        LocalDateTime base = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);

        publish(availabilityDao, analyst, base.withHour(9));
        publish(availabilityDao, analyst, base.withHour(15));
        publish(availabilityDao, analyst, base.plusDays(1).withHour(11));
        publish(availabilityDao, developer, base.withHour(10));
        publish(availabilityDao, developer, base.plusDays(2).withHour(17));
    }

    private void publish(AvailabilityDao availabilityDao, Tutor tutor, LocalDateTime start)
            throws PersistenceException, OverlappingAvailabilityException {
        Availability availability = tutor.publishAvailability(IdGenerator.next(),
                new TimeSlot(start, LESSON_MINUTES));
        availabilityDao.save(availability);
    }
}
