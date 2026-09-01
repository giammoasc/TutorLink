package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;
import java.util.regex.Pattern;

public class BookingRequestBean extends AbstractBean {
    private static final Pattern CARD_PATTERN = Pattern.compile("^\\d{16}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");

    private long availabilityId;
    private String subject;
    private String cardHolder;
    private String cardNumber;
    private String cardExpiry;

    public BookingRequestBean() {
        super();
    }

    public long getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    @Override
    public void validateSyntax() throws ValidationException {
        if (availabilityId <= 0) {
            throw new ValidationException("availabilityId", "no time slot has been selected");
        }
        this.subject = requireText(subject, "subject");
        this.cardHolder = requireText(cardHolder, "card holder");
        String digits = requireText(cardNumber, "card number").replace(" ", "");
        if (!CARD_PATTERN.matcher(digits).matches()) {
            throw new ValidationException("cardNumber", "the card number must be made of 16 digits");
        }
        this.cardNumber = digits;
        this.cardExpiry = requireText(cardExpiry, "expiry date");
        if (!EXPIRY_PATTERN.matcher(cardExpiry).matches()) {
            throw new ValidationException("cardExpiry", "the expiry date must be in the format MM/YY");
        }
    }
}
