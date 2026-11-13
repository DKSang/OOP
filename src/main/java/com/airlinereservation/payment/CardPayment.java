package com.airlinereservation.payment;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.exception.ValidationException;

/**
 * Payment strategy processing credit or debit card transactions.
 */
public class CardPayment implements PaymentStrategy {

    private final String cardNumber;
    private final String cardHolderName;

    public CardPayment(String cardNumber, String cardHolderName) {
        this.cardNumber = ValidationException.requireNonBlank(cardNumber, "Card number");
        this.cardHolderName = ValidationException.requireNonBlank(cardHolderName, "Card holder name");
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CREDIT_DEBIT_CARD;
    }

    @Override
    public boolean pay(double amount) {
        return PaymentStrategy.isPayableAmount(amount);
    }
}
