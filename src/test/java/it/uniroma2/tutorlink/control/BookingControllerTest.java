package it.uniroma2.tutorlink.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.bean.BookingRequestBean;
import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.dao.memory.InMemoryDaoFactory;
import it.uniroma2.tutorlink.exception.PaymentFailedException;
import it.uniroma2.tutorlink.exception.SlotUnavailableException;
import it.uniroma2.tutorlink.external.GoogleMeetAdapter;
import it.uniroma2.tutorlink.external.SimulatedPaymentGateway;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.matching.AdaptiveMatchingStrategy;
import it.uniroma2.tutorlink.model.matching.BestTimeBandSlotStrategy;
import it.uniroma2.tutorlink.session.SessionManager;
import it.uniroma2.tutorlink.support.RecordingNotificationSender;
import it.uniroma2.tutorlink.support.TestFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulla prenotazione, compresi i casi di errore.
// In carico a: Cicerchia Nicolas
class BookingControllerTest {
    private InMemoryDaoFactory daos;
    private Student student;
    private Tutor tutor;
    private Availability availability;
    private RecordingNotificationSender recorder;
    private BookingController controller;
    private GoogleMeetAdapter meetAdapter;

    @BeforeEach
    void setUp() throws Exception {
        daos = TestFixture.freshDaos();
        student = TestFixture.student(daos, "m.rossi@students.uniroma2.eu", "Mario Rossi");
        tutor = TestFixture.tutor(daos, "g.bianchi@uniroma2.eu", "Giulia Bianchi", 30,
                Subject.MATHEMATICS);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10)
                .withMinute(0).withSecond(0).withNano(0);
        availability = tutor.publishAvailability(1L, new TimeSlot(start, 60));
        daos.createAvailabilityDao().save(availability);

        recorder = new RecordingNotificationSender();
        meetAdapter = new GoogleMeetAdapter();
        controller = new BookingController(daos, new SimulatedPaymentGateway(), meetAdapter,
                recorder, new AdaptiveMatchingStrategy(), new BestTimeBandSlotStrategy());
        SessionManager.getInstance().open(student);
    }

    private BookingRequestBean request(String cardNumber) {
        BookingRequestBean request = new BookingRequestBean();
        request.setAvailabilityId(availability.id());
        request.setSubject(Subject.MATHEMATICS.displayName());
        request.setCardHolder("Mario Rossi");
        request.setCardNumber(cardNumber);
        request.setCardExpiry("12/30");
        return request;
    }

    @Test
    @DisplayName("steps 4 and 6: the tutor is proposed and only his free slot is offered")
    void tutorsAndSlotsAreProposed() throws Exception {
        assertEquals(1, controller.rankedTutors(Subject.MATHEMATICS.displayName()).size());

        List<AvailabilityBean> slots = controller.freeSlots(tutor.email());
        assertEquals(1, slots.size());
        assertEquals(availability.id(), slots.get(0).getId());
        assertFalse(slots.get(0).isReserved());
    }

    @Test
    @DisplayName("steps 7 to 12: the lesson is confirmed, gets a link and the tutor is notified")
    void happyPath() throws Exception {
        LessonBean booked = controller.confirmBooking(request(TestFixture.APPROVED_CARD));

        assertEquals("CONFIRMED", booked.getState());
        assertTrue(booked.getMeetingLink().startsWith("https://meet.google.com/"));
        assertTrue(availability.isReserved());
        assertEquals(1, recorder.count(), "the tutor has to receive the notification");
        assertEquals(tutor, recorder.sent().get(0).recipient());
        assertTrue(controller.freeSlots(tutor.email()).isEmpty(), "the slot is not offered any more");
    }

    @Test
    @DisplayName("alternative flow 10a: a refused payment releases the slot and cancels the lesson")
    void refusedPaymentIsCompensated() throws Exception {
        assertThrows(PaymentFailedException.class,
                () -> controller.confirmBooking(request(TestFixture.REFUSED_CARD)));

        assertFalse(availability.isReserved(), "the slot has to go back on the market");
        List<Lesson> lessons = daos.createLessonDao().findByStudent(student);
        assertEquals(1, lessons.size());
        assertEquals("CANCELLED", lessons.get(0).stateName());
        assertEquals(0, recorder.count(), "no tutor is notified for a failed booking");
    }

    @Test
    @DisplayName("alternative flow 7a: a slot taken in the meantime is refused")
    void takenSlotIsRefused() throws Exception {
        availability.reserve();
        SlotUnavailableException thrown = assertThrows(SlotUnavailableException.class,
                () -> controller.confirmBooking(request(TestFixture.APPROVED_CARD)));
        assertEquals(availability.id(), thrown.getSlotId());
    }

    @Test
    @DisplayName("alternative flow 11a: an unreachable calendar service does not break the booking")
    void meetingServiceOutageIsDegraded() throws Exception {
        meetAdapter.simulateOutage(true);
        LessonBean booked = controller.confirmBooking(request(TestFixture.APPROVED_CARD));

        assertEquals("CONFIRMED", booked.getState());
        assertTrue(booked.getMeetingLink().isEmpty());
        Lesson stored = daos.createLessonDao().findById(booked.getId()).orElseThrow();
        assertTrue(stored.needsDeferredLink(), "the link generation has to be retried at join time");
    }

    @Test
    @DisplayName("the student cannot book two lessons on overlapping time slots")
    void overlappingBookingIsRefused() throws Exception {
        controller.confirmBooking(request(TestFixture.APPROVED_CARD));

        Tutor other = TestFixture.tutor(daos, "l.verdi@uniroma2.eu", "Luca Verdi", 25,
                Subject.MATHEMATICS);
        Availability second = other.publishAvailability(2L,
                new TimeSlot(availability.slot().start().plusMinutes(30), 60));
        daos.createAvailabilityDao().save(second);

        BookingRequestBean overlapping = request(TestFixture.APPROVED_CARD);
        overlapping.setAvailabilityId(second.id());
        assertThrows(SlotUnavailableException.class, () -> controller.confirmBooking(overlapping));
    }
}
