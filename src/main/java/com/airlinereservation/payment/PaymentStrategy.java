package com.airlinereservation.payment;

/**
 * Strategy interface defining payment execution for a transaction amount.
 */
@FunctionalInterface
public interface PaymentStrategy {

    boolean pay(double amount);

    static boolean isPayableAmount(double amount) {
        return Double.isFinite(amount) && amount > 0;
    }
}
