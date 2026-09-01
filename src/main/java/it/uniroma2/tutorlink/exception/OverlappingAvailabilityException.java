package it.uniroma2.tutorlink.exception;

public class OverlappingAvailabilityException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    public OverlappingAvailabilityException(String message) {
        super(message);
    }

    public OverlappingAvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
