package com.airlinereservation.model;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.PaymentException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.payment.CardPayment;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-11-13T16:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZONE);

    @Test
    void processesPendingPaymentToCompletedWithClockTimestampAndRejectsReprocessing() {
        Payment payment = new Payment(" PAY-01 ", 2_000_000.0, PaymentMethod.CREDIT_DEBIT_CARD, FIXED_CLOCK);

        assertEquals("PAY-01", payment.getPaymentId());
        assertEquals(2_000_000.0, payment.getAmount(), 0.001);
        assertEquals(PaymentMethod.CREDIT_DEBIT_CARD, payment.getMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNull(payment.getPaidAt());

        assertTrue(payment.processPayment(new CardPayment("4111-2222-3333-4444", "Nguyen Van A")));
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(LocalDateTime.ofInstant(FIXED_INSTANT, ZONE), payment.getPaidAt());

        assertThrows(PaymentException.class, () -> payment.processPayment(amount -> true));
    }

    @Test
    void marksPaymentFailedWhenStrategyDeclinesAndRejectsInvalidArguments() {
        Payment failedPayment = new Payment("PAY-02", 1_500_000.0, PaymentMethod.E_WALLET, FIXED_CLOCK);

        assertFalse(failedPayment.processPayment(amount -> false));
        assertEquals(PaymentStatus.FAILED, failedPayment.getStatus());
        assertNull(failedPayment.getPaidAt());

        assertThrows(PaymentException.class, () -> failedPayment.processPayment(amount -> true));
        assertThrows(ValidationException.class, () -> failedPayment.processPayment(null));

        assertThrows(ValidationException.class, () -> new Payment(" ", 100.0, PaymentMethod.CASH));
        assertThrows(ValidationException.class, () -> new Payment("PAY-3", 0.0, PaymentMethod.CASH));
        assertThrows(ValidationException.class, () -> new Payment("PAY-3", -50.0, PaymentMethod.CASH));
        assertThrows(ValidationException.class, () -> new Payment("PAY-3", Double.NaN, PaymentMethod.CASH));
        assertThrows(ValidationException.class, () -> new Payment("PAY-3", 100.0, null));
        assertThrows(ValidationException.class, () -> new Payment("PAY-3", 100.0, PaymentMethod.CASH, null));
    }
}
