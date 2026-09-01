package it.uniroma2.tutorlink.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// Intervallo di tempo. Sa dire da solo se si sovrappone a un altro.
public final class TimeSlot implements Comparable<TimeSlot> {
    public static final int MIN_MINUTES = 30;
    public static final int MAX_MINUTES = 240;

    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final LocalDateTime start;
    private final int minutes;

    public TimeSlot(LocalDateTime start, int minutes) {
        Objects.requireNonNull(start, "start");
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException(
                    "the duration of a slot must be between " + MIN_MINUTES + " and " + MAX_MINUTES + " minutes");
        }
        if (minutes % MIN_MINUTES != 0) {
            throw new IllegalArgumentException("the duration of a slot must be a multiple of " + MIN_MINUTES);
        }
        this.start = start.withSecond(0).withNano(0);
        this.minutes = minutes;
    }

    public LocalDateTime start() {
        return start;
    }

    public LocalDateTime end() {
        return start.plusMinutes(minutes);
    }

    public int minutes() {
        return minutes;
    }

    public LocalDate date() {
        return start.toLocalDate();
    }

    // due slot si sovrappongono se condividono almeno un istante
    public boolean overlaps(TimeSlot other) {
        Objects.requireNonNull(other, "other");
        return start.isBefore(other.end()) && other.start.isBefore(this.end());
    }

    public boolean contains(LocalDateTime instant) {
        return !instant.isBefore(start) && instant.isBefore(end());
    }

    public boolean isInThePast(LocalDateTime reference) {
        return end().isBefore(reference);
    }

    public boolean startsWithin(LocalDateTime from, LocalDateTime to) {
        return !start.isBefore(from) && !start.isAfter(to);
    }

    public long hoursBefore(LocalDateTime reference) {
        return Duration.between(reference, start).toHours();
    }

    @Override
    public int compareTo(TimeSlot other) {
        int byStart = this.start.compareTo(other.start);
        return byStart != 0 ? byStart : Integer.compare(this.minutes, other.minutes);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TimeSlot)) {
            return false;
        }
        TimeSlot slot = (TimeSlot) other;
        return minutes == slot.minutes && start.equals(slot.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, minutes);
    }

    @Override
    public String toString() {
        return DISPLAY.format(start) + " (" + minutes + " min)";
    }
}
