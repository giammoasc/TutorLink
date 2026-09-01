package it.uniroma2.tutorlink.bean;

public class NotificationBean extends AbstractBean {
    private long id;
    private String message;
    private String createdAt;
    private boolean seen;

    public NotificationBean() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public void validateSyntax() {
        // bean di sola uscita: non c’è niente da controllare
    }

    @Override
    public String toString() {
        return (seen ? "   " : " * ") + createdAt + "  " + message;
    }
}
