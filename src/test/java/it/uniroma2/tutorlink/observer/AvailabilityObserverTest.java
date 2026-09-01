package it.uniroma2.tutorlink.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.control.AvailabilityController;
import it.uniroma2.tutorlink.dao.cache.DaoCacheInvalidator;
import it.uniroma2.tutorlink.dao.memory.InMemoryDaoFactory;
import it.uniroma2.tutorlink.exception.OverlappingAvailabilityException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.state.LessonStates;
import it.uniroma2.tutorlink.session.SessionManager;
import it.uniroma2.tutorlink.support.RecordingCache;
import it.uniroma2.tutorlink.support.RecordingNotificationSender;
import it.uniroma2.tutorlink.support.TestFixture;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulla notifica di una nuova disponibilita'.
// In carico a: Ascenzi Gianmarco
class AvailabilityObserverTest {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private InMemoryDaoFactory daos;
    private Tutor tutor;
    private RecordingNotificationSender recorder;
    private RecordingCache cache;
    private AvailabilityController controller;

    @BeforeEach
    void setUp() throws Exception {
        daos = TestFixture.freshDaos();
        Student student = TestFixture.student(daos, "m.rossi@students.uniroma2.eu", "Mario Rossi");
        tutor = TestFixture.tutor(daos, "g.bianchi@uniroma2.eu", "Giulia Bianchi", 28,
                Subject.MATHEMATICS);

        LocalDateTime past = LocalDateTime.now().minusDays(10).withHour(10)
                .withMinute(0).withSecond(0).withNano(0);
        Lesson delivered = new Lesson(1L, student, tutor, Subject.MATHEMATICS,
                new TimeSlot(past, 60), Money.of(28), null, LessonStates.of("COMPLETED"), null);
        daos.createLessonDao().save(delivered);

        recorder = new RecordingNotificationSender();
        cache = new RecordingCache();

        AvailabilityPublisher publisher = AvailabilityPublisher.getInstance();
        publisher.attach(new StudentNotifier(daos.createLessonDao(), recorder));
        publisher.attach(new DaoCacheInvalidator(List.of(cache)));

        controller = new AvailabilityController(daos, publisher);
        SessionManager.getInstance().open(tutor);
    }

    private AvailabilityBean slotRequest(LocalDateTime start) {
        AvailabilityBean bean = new AvailabilityBean();
        bean.setDate(DATE.format(start));
        bean.setTime(TIME.format(start));
        bean.setMinutes("60");
        return bean;
    }

    @Test
    @DisplayName("publishing a slot notifies every student of that tutor and drops the caches")
    void publicationTriggersBothObservers() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(9)
                .withMinute(0).withSecond(0).withNano(0);

        controller.publish(slotRequest(start));

        assertEquals(1, recorder.count());
        assertTrue(recorder.sent().get(0).message().contains("Giulia Bianchi"));
        assertEquals(1, cache.invalidations());
        assertEquals(1, controller.myAvailabilities().size());
    }

    @Test
    @DisplayName("a slot colliding with an existing one is refused and nothing is notified")
    void collidingSlotNotifiesNobody() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(15)
                .withMinute(0).withSecond(0).withNano(0);
        controller.publish(slotRequest(start));
        int notificationsAfterFirst = recorder.count();

        assertThrows(OverlappingAvailabilityException.class,
                () -> controller.publish(slotRequest(start.plusMinutes(30))));

        assertEquals(notificationsAfterFirst, recorder.count());
        assertEquals(1, controller.myAvailabilities().size());
    }

    @Test
    @DisplayName("the publisher survives an observer that explodes")
    void aFailingObserverDoesNotBreakThePublication() throws Exception {
        AvailabilityPublisher.getInstance().attach(availability -> {
            throw new IllegalStateException("boom");
        });
        LocalDateTime start = LocalDateTime.now().plusDays(4).withHour(11)
                .withMinute(0).withSecond(0).withNano(0);

        controller.publish(slotRequest(start));

        assertEquals(1, recorder.count(), "the healthy observers still run");
    }
}
