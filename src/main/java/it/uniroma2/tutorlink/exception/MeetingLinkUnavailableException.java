package it.uniroma2.tutorlink.exception;

public class MeetingLinkUnavailableException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    public MeetingLinkUnavailableException(String message) {
        super(message);
    }

    public MeetingLinkUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
