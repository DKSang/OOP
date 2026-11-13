package com.airlinereservation.payment;

import com.airlinereservation.exception.ValidationException;

/**
 * Payment strategy processing counter cash transactions.
 */
public class CashPayment implements PaymentStrategy {

    private final String counterCode;
    private final String cashierId;

    public CashPayment(String counterCode, String cashierId) {
        this.counterCode = ValidationException.requireNonBlank(counterCode, "Counter code");
        this.cashierId = ValidationException.requireNonBlank(cashierId, "Cashier ID");
    }

    public String getCounterCode() {
        return counterCode;
    }

    public String getCashierId() {
        return cashierId;
    }

    @Override
    public boolean pay(double amount) {
        return PaymentStrategy.isPayableAmount(amount);
    }
}
