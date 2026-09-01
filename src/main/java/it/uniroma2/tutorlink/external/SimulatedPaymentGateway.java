package it.uniroma2.tutorlink.external;

import it.uniroma2.tutorlink.exception.PaymentFailedException;
import it.uniroma2.tutorlink.model.Money;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

// Pagamento simulato: approva, rifiuta o risulta non raggiungibile.
public class SimulatedPaymentGateway implements PaymentGateway {
    private static final Logger LOGGER = Logger.getLogger(SimulatedPaymentGateway.class.getName());

    private static final double CEILING = 500d;

    // carte di prova: finale 0000 rifiutata, finale 9999 servizio non raggiungibile
    private static final String REFUSED_SUFFIX = "0000";
    private static final String UNAVAILABLE_SUFFIX = "9999";

    @Override
    public PaymentReceipt charge(Money amount, PaymentCard card) throws PaymentFailedException {
        if (card.number().endsWith(REFUSED_SUFFIX)) {
            throw new PaymentFailedException("the card " + card.masked() + " was refused", "CARD_REFUSED");
        }
        if (card.number().endsWith(UNAVAILABLE_SUFFIX)) {
            throw new PaymentFailedException("the payment service is temporarily unreachable", "TEMPORARY_FAILURE");
        }
        if (amount.amount().doubleValue() > CEILING) {
            throw new PaymentFailedException(
                    "the amount " + amount + " exceeds the limit of the card", "LIMIT_EXCEEDED");
        }
        return new PaymentReceipt(UUID.randomUUID().toString(), amount, LocalDateTime.now());
    }

    @Override
    public void refund(PaymentReceipt receipt) {
        LOGGER.log(Level.INFO, "refund of {0} issued for the transaction {1}",
                new Object[]{receipt.amount(), receipt.transactionId()});
    }
}
