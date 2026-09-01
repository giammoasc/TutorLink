package it.uniroma2.tutorlink.exception;

public class PersistenceException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
