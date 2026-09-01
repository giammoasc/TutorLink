package it.uniroma2.tutorlink.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.exception.OverlappingAvailabilityException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sul calendario del tutor.
// In carico a: Cicerchia Nicolas
class TutorCalendarTest {
    private static final LocalDateTime TOMORROW_NINE =
            LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

    private Tutor newTutor() {
        Tutor tutor = new Tutor("g.bianchi@uniroma2.eu", "Giulia Bianchi", "salt:hash", Money.of(30));
        tutor.teach(Subject.MATHEMATICS);
        return tutor;
    }

    @Test
    @DisplayName("an overlapping availability is refused by the tutor himself")
    void overlappingAvailabilityIsRefused() throws OverlappingAvailabilityException {
        Tutor tutor = newTutor();
        tutor.publishAvailability(1L, new TimeSlot(TOMORROW_NINE, 60));

        assertThrows(OverlappingAvailabilityException.class,
                () -> tutor.publishAvailability(2L, new TimeSlot(TOMORROW_NINE.plusMinutes(30), 60)));
        assertEquals(1, tutor.availabilities().size());
    }

    @Test
    @DisplayName("adjacent availabilities are accepted and the price follows the duration")
    void adjacentAvailabilitiesAreAccepted() throws OverlappingAvailabilityException {
        Tutor tutor = newTutor();
        tutor.publishAvailability(1L, new TimeSlot(TOMORROW_NINE, 60));
        tutor.publishAvailability(2L, new TimeSlot(TOMORROW_NINE.plusHours(1), 30));

        assertEquals(2, tutor.availabilities().size());
        assertEquals(Money.of(30), tutor.priceFor(new TimeSlot(TOMORROW_NINE, 60)));
        assertEquals(Money.of(15), tutor.priceFor(new TimeSlot(TOMORROW_NINE, 30)));
    }

    @Test
    @DisplayName("only the free future slots inside the window are proposed")
    void freeAvailabilitiesAreFiltered() throws Exception {
        Tutor tutor = newTutor();
        Availability first = tutor.publishAvailability(1L, new TimeSlot(TOMORROW_NINE, 60));
        tutor.publishAvailability(2L, new TimeSlot(TOMORROW_NINE.plusHours(2), 60));
        first.reserve();

        List<Availability> free = tutor.freeAvailabilities(TOMORROW_NINE.toLocalDate(),
                TOMORROW_NINE.toLocalDate().plusDays(30), LocalDateTime.now());

        assertEquals(1, free.size());
        assertTrue(free.get(0).slot().start().isEqual(TOMORROW_NINE.plusHours(2)));
    }
}
