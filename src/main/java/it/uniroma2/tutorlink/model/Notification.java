package it.uniroma2.tutorlink.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Notification {
    private final long id;
    private final User recipient;
    private final String message;
    private final LocalDateTime createdAt;
    private boolean seen;

    public Notification(long id, User recipient, String message, LocalDateTime createdAt) {
        this.id = id;
        this.recipient = Objects.requireNonNull(recipient, "recipient");
        this.message = Objects.requireNonNull(message, "message");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public Notification(long id, User recipient, String message, LocalDateTime createdAt, boolean seen) {
        this(id, recipient, message, createdAt);
        this.seen = seen;
    }

    public long id() {
        return id;
    }

    public User recipient() {
        return recipient;
    }

    public String message() {
        return message;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public boolean isSeen() {
        return seen;
    }

    public void markSeen() {
        this.seen = true;
    }

    @Override
    public String toString() {
        return (seen ? "  " : "* ") + message;
    }
}
