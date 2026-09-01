package it.uniroma2.tutorlink.model;

import it.uniroma2.tutorlink.exception.SlotUnavailableException;
import java.time.LocalDateTime;
import java.util.Objects;

// Slot pubblicato da un tutor. Solo lui decide se e' gia' prenotato.
public class Availability {
    private final long id;
    private final Tutor tutor;
    private final TimeSlot slot;
    private boolean reserved;

    public Availability(long id, Tutor tutor, TimeSlot slot) {
        this.id = id;
        this.tutor = Objects.requireNonNull(tutor, "tutor");
        this.slot = Objects.requireNonNull(slot, "slot");
    }

    public Availability(long id, Tutor tutor, TimeSlot slot, boolean reserved) {
        this(id, tutor, slot);
        this.reserved = reserved;
    }

    public long id() {
        return id;
    }

    public Tutor tutor() {
        return tutor;
    }

    public TimeSlot slot() {
        return slot;
    }

    public boolean isReserved() {
        return reserved;
    }

    public boolean isBookableAt(LocalDateTime now) {
        return !reserved && slot.start().isAfter(now);
    }

    public void reserve() throws SlotUnavailableException {
        if (reserved) {
            throw new SlotUnavailableException(
                    "the slot of " + slot + " is not available any more", id);
        }
        reserved = true;
    }

    public void release() {
        reserved = false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Availability)) {
            return false;
        }
        Availability availability = (Availability) other;
        return id == availability.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return slot + (reserved ? " [reserved]" : " [free]");
    }
}
