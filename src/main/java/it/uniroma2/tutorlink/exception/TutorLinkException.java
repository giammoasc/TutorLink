package it.uniroma2.tutorlink.exception;

public class TutorLinkException extends Exception {
    private static final long serialVersionUID = 1L;

    protected TutorLinkException(String message) {
        super(message);
    }

    protected TutorLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
