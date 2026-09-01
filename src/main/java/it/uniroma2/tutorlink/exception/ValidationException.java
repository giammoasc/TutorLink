package it.uniroma2.tutorlink.exception;

// Campo compilato male: e' l'unica eccezione che nasce nella GUI.
public class ValidationException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
