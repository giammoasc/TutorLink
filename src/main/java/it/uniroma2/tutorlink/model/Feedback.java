package it.uniroma2.tutorlink.model;

import java.time.LocalDateTime;
import java.util.Objects;

// Voto e commento che il tutor da' allo studente a fine lezione.
public class Feedback {
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 10;

    private final Lesson lesson;
    private final int score;
    private final String comment;
    private final LocalDateTime createdAt;

    public Feedback(Lesson lesson, int score, String comment, LocalDateTime createdAt) {
        this.lesson = Objects.requireNonNull(lesson, "lesson");
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(
                    "the score must be between " + MIN_SCORE + " and " + MAX_SCORE);
        }
        this.score = score;
        this.comment = comment == null ? "" : comment.trim();
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public Lesson lesson() {
        return lesson;
    }

    public int score() {
        return score;
    }

    public String comment() {
        return comment;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return score + "/" + MAX_SCORE + (comment.isEmpty() ? "" : " - " + comment);
    }
}
