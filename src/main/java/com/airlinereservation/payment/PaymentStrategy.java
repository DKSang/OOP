package com.airlinereservation.payment;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.exception.ValidationException;

/**
 * Strategy interface defining payment execution for a transaction amount.
 */
@FunctionalInterface
public interface PaymentStrategy {

    boolean pay(double amount);

    /**
     * Returns the {@link PaymentMethod} represented by this strategy, or {@code null} for generic lambdas.
     */
    default PaymentMethod getPaymentMethod() {
        return null;
    }

    /**
     * Returns whether this strategy is compatible with the given {@link PaymentMethod}.
     */
    default boolean supports(PaymentMethod method) {
        if (method == null) {
            return false;
        }
        PaymentMethod declared = getPaymentMethod();
        return declared == null || declared == method;
    }

    static boolean isPayableAmount(double amount) {
        return Double.isFinite(amount) && amount > 0;
    }

    static PaymentStrategy requireSupports(PaymentStrategy strategy, PaymentMethod method) {
        ValidationException.requireNonNull(strategy, "Payment strategy");
        if (!strategy.supports(method)) {
            throw new ValidationException("Payment strategy " + strategy.getPaymentMethod()
                    + " does not support payment method: " + method);
        }
        return strategy;
    }
}
