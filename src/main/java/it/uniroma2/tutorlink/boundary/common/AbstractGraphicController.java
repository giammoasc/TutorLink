package it.uniroma2.tutorlink.boundary.common;

import it.uniroma2.tutorlink.exception.MaterialRejectedException;
import it.uniroma2.tutorlink.exception.PaymentFailedException;
import it.uniroma2.tutorlink.exception.SlotUnavailableException;
import it.uniroma2.tutorlink.exception.TutorLinkException;
import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import it.uniroma2.tutorlink.exception.ValidationException;

// Base dei controller grafici: trasforma le eccezioni in messaggi per l'utente.
public abstract class AbstractGraphicController {
    private final Navigator navigator;

    protected AbstractGraphicController(Navigator navigator) {
        this.navigator = navigator;
    }

    protected Navigator navigator() {
        return navigator;
    }

    protected void report(TutorLinkException exception) {
        if (exception instanceof ValidationException) {
            ValidationException validation = (ValidationException) exception;
            navigator.error("Check the field '" + validation.getField() + "'", exception.getMessage());
            return;
        }
        if (exception instanceof SlotUnavailableException) {
            navigator.error("The time slot is gone",
                    exception.getMessage() + "\nThe calendar has been refreshed, please pick another slot.");
            return;
        }
        if (exception instanceof PaymentFailedException) {
            PaymentFailedException payment = (PaymentFailedException) exception;
            navigator.error("Payment refused", exception.getMessage()
                    + (payment.isRetryable()
                        ? "\nThe service is temporarily unavailable, you can try again in a moment."
                        : "\nThe reservation has been released, no amount has been charged."));
            return;
        }
        if (exception instanceof UnsupportedMaterialFormatException) {
            UnsupportedMaterialFormatException format = (UnsupportedMaterialFormatException) exception;
            navigator.error("File not accepted",
                    exception.getMessage() + "\nAccepted formats: " + String.join(", ", format.getAcceptedFormats()));
            return;
        }
        if (exception instanceof MaterialRejectedException) {
            navigator.error("File not accepted", exception.getMessage());
            return;
        }
        navigator.error("Operation not completed", exception.getMessage());
    }
}
