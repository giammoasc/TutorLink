package it.uniroma2.tutorlink.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// Importo in euro, immutabile e sempre a due decimali.
public final class Money implements Comparable<Money> {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("a monetary amount cannot be negative");
        }
        return new Money(amount);
    }

    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }

    public Money proRata(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes must be positive");
        }
        BigDecimal factor = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
        return new Money(amount.multiply(factor));
    }


    public BigDecimal amount() {
        return amount;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money)) {
            return false;
        }
        Money money = (Money) other;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%.2f EUR", amount);
    }
}
