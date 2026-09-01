package it.uniroma2.tutorlink.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulle sovrapposizioni fra slot.
// In carico a: Cicerchia Nicolas
class TimeSlotTest {
    private static final LocalDateTime NINE = LocalDateTime.of(2026, 9, 21, 9, 0);

    @Test
    @DisplayName("two slots sharing an instant overlap, two adjacent slots do not")
    void overlapIsHalfOpen() {
        TimeSlot nineToTen = new TimeSlot(NINE, 60);
        TimeSlot halfPastNineToHalfPastTen = new TimeSlot(NINE.plusMinutes(30), 60);
        TimeSlot tenToEleven = new TimeSlot(NINE.plusHours(1), 60);

        assertTrue(nineToTen.overlaps(halfPastNineToHalfPastTen));
        assertTrue(halfPastNineToHalfPastTen.overlaps(nineToTen));
        assertFalse(nineToTen.overlaps(tenToEleven), "a slot that starts exactly at the end must not overlap");
        assertFalse(tenToEleven.overlaps(nineToTen));
    }

    @Test
    @DisplayName("the duration of a slot is constrained")
    void durationIsValidated() {
        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(NINE, 20));
        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(NINE, 300));
        assertThrows(IllegalArgumentException.class, () -> new TimeSlot(NINE, 45));
        assertEquals(60, new TimeSlot(NINE, 60).minutes());
    }

    @Test
    @DisplayName("the end of a slot and the hours before it are computed from the duration")
    void endAndDistance() {
        TimeSlot slot = new TimeSlot(NINE, 90);
        assertEquals(NINE.plusMinutes(90), slot.end());
        assertEquals(9, slot.hoursBefore(NINE.minusHours(9)));
        assertTrue(slot.contains(NINE.plusMinutes(89)));
        assertFalse(slot.contains(slot.end()));
    }
}
