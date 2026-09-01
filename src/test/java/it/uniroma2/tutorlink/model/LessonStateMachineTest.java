package it.uniroma2.tutorlink.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.exception.SlotUnavailableException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulle transizioni di stato della lezione.
// In carico a: Cicerchia Nicolas
class LessonStateMachineTest {
    private static final LocalDateTime START =
            LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);

    private Lesson newLesson() throws SlotUnavailableException {
        Student student = new Student("m.rossi@students.uniroma2.eu", "Mario Rossi", "salt:hash");
        Tutor tutor = new Tutor("g.bianchi@uniroma2.eu", "Giulia Bianchi", "salt:hash", Money.of(30));
        tutor.teach(Subject.MATHEMATICS);
        Availability availability = new Availability(1L, tutor, new TimeSlot(START, 60));
        return Lesson.schedule(10L, student, availability, Subject.MATHEMATICS);
    }

    @Test
    @DisplayName("a new lesson reserves the slot and waits for the payment")
    void bookingReservesTheSlot() throws SlotUnavailableException {
        Lesson lesson = newLesson();
        assertEquals("PENDING_PAYMENT", lesson.stateName());
        assertTrue(lesson.origin().orElseThrow().isReserved());
        assertFalse(lesson.state().allowsMaterial(), "no material before the payment");
    }

    @Test
    @DisplayName("the payment confirms the lesson, a second confirmation is illegal")
    void paymentConfirmsOnce() throws SlotUnavailableException, IllegalLessonStateException {
        Lesson lesson = newLesson();
        lesson.confirmPayment();

        assertEquals("CONFIRMED", lesson.stateName());
        assertTrue(lesson.state().allowsMaterial());
        assertThrows(IllegalLessonStateException.class, lesson::confirmPayment);
    }

    @Test
    @DisplayName("cancelling before the payment gives the slot back")
    void cancellationReleasesTheSlot() throws SlotUnavailableException, IllegalLessonStateException {
        Lesson lesson = newLesson();
        lesson.cancel("payment refused", LocalDateTime.now());

        assertEquals("CANCELLED", lesson.stateName());
        assertTrue(lesson.isTerminal());
        assertFalse(lesson.origin().orElseThrow().isReserved(), "the slot has to be bookable again");
    }

    @Test
    @DisplayName("a confirmed lesson cannot be cancelled less than 24 hours before it starts")
    void lateCancellationIsRefused() throws SlotUnavailableException, IllegalLessonStateException {
        Lesson lesson = newLesson();
        lesson.confirmPayment();
        LocalDateTime tooLate = lesson.slot().start().minusHours(3);

        assertThrows(IllegalLessonStateException.class, () -> lesson.cancel("changed my mind", tooLate));
        assertEquals("CONFIRMED", lesson.stateName());
    }

    @Test
    @DisplayName("the lesson can be started only inside its own time window")
    void startIsBoundToTheSlot() throws SlotUnavailableException, IllegalLessonStateException {
        Lesson lesson = newLesson();
        lesson.confirmPayment();

        assertThrows(IllegalLessonStateException.class,
                () -> lesson.start(lesson.slot().start().minusHours(2)));

        lesson.start(lesson.slot().start().plusMinutes(5));
        assertEquals("IN_PROGRESS", lesson.stateName());

        Feedback feedback = new Feedback(lesson, 8, "well done", LocalDateTime.now());
        lesson.complete(feedback);
        assertEquals("COMPLETED", lesson.stateName());
        assertEquals(8, lesson.feedback().orElseThrow().score());
    }
}
