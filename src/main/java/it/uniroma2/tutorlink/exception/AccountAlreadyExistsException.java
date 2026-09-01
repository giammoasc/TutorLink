package it.uniroma2.tutorlink.exception;

public class AccountAlreadyExistsException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    public AccountAlreadyExistsException(String message) {
        super(message);
    }

    public AccountAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
