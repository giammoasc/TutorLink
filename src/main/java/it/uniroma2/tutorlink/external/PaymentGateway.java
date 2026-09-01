package it.uniroma2.tutorlink.external;

import it.uniroma2.tutorlink.exception.PaymentFailedException;
import it.uniroma2.tutorlink.model.Money;

public interface PaymentGateway {
    PaymentReceipt charge(Money amount, PaymentCard card) throws PaymentFailedException;

    void refund(PaymentReceipt receipt);
}
