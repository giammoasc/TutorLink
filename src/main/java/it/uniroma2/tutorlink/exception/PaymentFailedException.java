package it.uniroma2.tutorlink.exception;

// Pagamento rifiutato. Il codice dice se ha senso riprovare.
public class PaymentFailedException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    private final String reasonCode;

    public PaymentFailedException(String message, String reasonCode) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public boolean isRetryable() {
        return "TEMPORARY_FAILURE".equals(reasonCode);
    }
}
