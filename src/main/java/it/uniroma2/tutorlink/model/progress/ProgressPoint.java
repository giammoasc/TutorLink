package it.uniroma2.tutorlink.model.progress;

import it.uniroma2.tutorlink.model.Subject;
import java.time.LocalDate;
import java.util.Objects;

public final class ProgressPoint {
    private final LocalDate date;
    private final int hour;
    private final Subject subject;
    private final int score;
    private final String tutorName;

    public ProgressPoint(LocalDate date, int hour, Subject subject, int score, String tutorName) {
        this.date = Objects.requireNonNull(date, "date");
        this.subject = Objects.requireNonNull(subject, "subject");
        this.tutorName = Objects.requireNonNull(tutorName, "tutorName");
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hour must be between 0 and 23");
        }
        this.hour = hour;
        this.score = score;
    }

    public LocalDate date() {
        return date;
    }


    public Subject subject() {
        return subject;
    }

    public int score() {
        return score;
    }

    public String tutorName() {
        return tutorName;
    }

    public int timeBand() {
        return hour / 3 * 3;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProgressPoint)) {
            return false;
        }
        ProgressPoint point = (ProgressPoint) other;
        return hour == point.hour && score == point.score
                && date.equals(point.date) && subject == point.subject
                && tutorName.equals(point.tutorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, hour, subject, score, tutorName);
    }

    @Override
    public String toString() {
        return date + " " + subject.displayName() + ": " + score;
    }
}
