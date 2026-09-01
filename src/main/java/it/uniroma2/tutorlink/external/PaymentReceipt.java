package it.uniroma2.tutorlink.external;

import it.uniroma2.tutorlink.model.Money;
import java.time.LocalDateTime;
import java.util.Objects;

public final class PaymentReceipt {
    private final String transactionId;
    private final Money amount;
    private final LocalDateTime settledAt;

    public PaymentReceipt(String transactionId, Money amount, LocalDateTime settledAt) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.settledAt = Objects.requireNonNull(settledAt, "settledAt");
    }

    public String transactionId() {
        return transactionId;
    }

    public Money amount() {
        return amount;
    }

    public LocalDateTime settledAt() {
        return settledAt;
    }

    @Override
    public String toString() {
        return "transaction " + transactionId + " of " + amount;
    }
}
