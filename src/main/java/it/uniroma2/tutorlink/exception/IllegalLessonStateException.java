package it.uniroma2.tutorlink.exception;

public class IllegalLessonStateException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    public IllegalLessonStateException(String message) {
        super(message);
    }

    public IllegalLessonStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
