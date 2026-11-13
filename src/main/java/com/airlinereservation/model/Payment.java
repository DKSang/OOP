package com.airlinereservation.model;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.PaymentException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.payment.PaymentStrategy;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Payment transaction record tracking method, amount, execution status, and timestamp.
 */
public class Payment {

    private final String paymentId;
    private final double amount;
    private final PaymentMethod method;
    private final Clock clock;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    public Payment(String paymentId, double amount, PaymentMethod method) {
        this(paymentId, amount, method, Clock.systemDefaultZone());
    }

    public Payment(String paymentId, double amount, PaymentMethod method, Clock clock) {
        this.paymentId = ValidationException.requireNonBlank(paymentId, "Payment ID");
        this.amount = ValidationException.requirePositive(amount, "Payment amount");
        this.method = ValidationException.requireNonNull(method, "Payment method");
        this.clock = ValidationException.requireNonNull(clock, "Clock");
        this.status = PaymentStatus.PENDING;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public boolean processPayment(PaymentStrategy strategy) {
        ValidationException.requireNonNull(strategy, "Payment strategy");
        if (status != PaymentStatus.PENDING) {
            throw new PaymentException("Cannot process payment " + paymentId + " in status: " + status);
        }
        if (strategy.pay(amount)) {
            this.status = PaymentStatus.COMPLETED;
            this.paidAt = LocalDateTime.now(clock);
            return true;
        }
        this.status = PaymentStatus.FAILED;
        return false;
    }
}
