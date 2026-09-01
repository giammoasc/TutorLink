package it.uniroma2.tutorlink.model;

import it.uniroma2.tutorlink.exception.OverlappingAvailabilityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Tutor: gestisce il calendario e rifiuta le disponibilita' sovrapposte.
public class Tutor extends User {
    private final Set<Subject> subjects = EnumSet.noneOf(Subject.class);
    private final List<Availability> availabilities = new ArrayList<>();
    private final List<Lesson> lessons = new ArrayList<>();
    private Money hourlyRate;

    public Tutor(String email, String fullName, String passwordDigest, Money hourlyRate) {
        super(email, fullName, passwordDigest);
        this.hourlyRate = Objects.requireNonNull(hourlyRate, "hourlyRate");
    }

    @Override
    public UserRole role() {
        return UserRole.TUTOR;
    }

    public Money hourlyRate() {
        return hourlyRate;
    }


    public Money priceFor(TimeSlot slot) {
        return hourlyRate.proRata(slot.minutes());
    }

    public void teach(Subject subject) {
        subjects.add(subject);
    }

    public boolean teaches(Subject subject) {
        return subjects.contains(subject);
    }

    public Set<Subject> subjects() {
        return Collections.unmodifiableSet(subjects);
    }

    public Availability publishAvailability(long id, TimeSlot slot) throws OverlappingAvailabilityException {
        Objects.requireNonNull(slot, "slot");
        for (Availability published : availabilities) {
            if (published.slot().overlaps(slot)) {
                throw new OverlappingAvailabilityException(
                        "the slot " + slot + " overlaps the availability already published on "
                                + published.slot());
            }
        }
        boolean busy = lessons.stream()
                .filter(lesson -> !lesson.isTerminal())
                .anyMatch(lesson -> lesson.slot().overlaps(slot));
        if (busy) {
            throw new OverlappingAvailabilityException(
                    "the slot " + slot + " overlaps a lesson already scheduled");
        }
        Availability availability = new Availability(id, this, slot);
        availabilities.add(availability);
        return availability;
    }

    public void addAvailability(Availability availability) {
        if (!availabilities.contains(availability)) {
            availabilities.add(availability);
        }
    }

    public void assign(Lesson lesson) {
        if (!lessons.contains(lesson)) {
            lessons.add(lesson);
        }
    }

    public List<Availability> availabilities() {
        return Collections.unmodifiableList(availabilities);
    }

    public List<Lesson> lessons() {
        return Collections.unmodifiableList(lessons);
    }

    public List<Availability> freeAvailabilities(LocalDate from, LocalDate to, LocalDateTime now) {
        LocalDateTime lower = from.atStartOfDay();
        LocalDateTime upper = to.atTime(23, 59);
        return availabilities.stream()
                .filter(availability -> availability.isBookableAt(now))
                .filter(availability -> availability.slot().startsWithin(lower, upper))
                .sorted((a, b) -> a.slot().compareTo(b.slot()))
                .toList();
    }


    public int deliveredLessons() {
        return (int) lessons.stream().filter(lesson -> lesson.feedback().isPresent()).count();
    }
}
