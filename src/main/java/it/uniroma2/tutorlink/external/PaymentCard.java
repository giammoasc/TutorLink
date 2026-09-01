package it.uniroma2.tutorlink.external;

import java.util.Objects;

// Carta di pagamento. Fuori esce solo il numero mascherato.
public final class PaymentCard {
    private static final int VISIBLE_DIGITS = 4;

    private final String holder;
    private final String number;
    private final String expiry;

    public PaymentCard(String holder, String number, String expiry) {
        this.holder = Objects.requireNonNull(holder, "holder");
        this.number = Objects.requireNonNull(number, "number");
        this.expiry = Objects.requireNonNull(expiry, "expiry");
    }

    public String holder() {
        return holder;
    }

    public String number() {
        return number;
    }

    public String expiry() {
        return expiry;
    }

    public String masked() {
        int length = number.length();
        return length <= VISIBLE_DIGITS
                ? "****"
                : "**** **** **** " + number.substring(length - VISIBLE_DIGITS);
    }

    @Override
    public String toString() {
        return holder + " " + masked();
    }
}
