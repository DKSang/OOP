package com.airlinereservation.payment;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Payment;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentFactoryTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-11-14T01:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZONE);

    @Test
    void createsPaymentsWithDefaultAndCustomClockAndValidatesInputs() {
        Payment defaultClockPayment = PaymentFactory.createPayment(" PAY-100 ", 1_200_000.0, PaymentMethod.CASH);
        assertEquals("PAY-100", defaultClockPayment.getPaymentId());
        assertEquals(1_200_000.0, defaultClockPayment.getAmount(), 0.001);
        assertEquals(PaymentMethod.CASH, defaultClockPayment.getMethod());
        assertEquals(PaymentStatus.PENDING, defaultClockPayment.getStatus());
        assertNull(defaultClockPayment.getPaidAt());

        Payment fixedClockPayment = PaymentFactory.createPayment(
                "PAY-101", 2_500_000.0, PaymentMethod.CREDIT_DEBIT_CARD, FIXED_CLOCK);
        assertTrue(fixedClockPayment.processPayment(amount -> true));
        assertEquals(LocalDateTime.ofInstant(FIXED_INSTANT, ZONE), fixedClockPayment.getPaidAt());

        assertThrows(ValidationException.class,
                () -> PaymentFactory.createPayment(" ", 100.0, PaymentMethod.CASH));
        assertThrows(ValidationException.class,
                () -> PaymentFactory.createPayment("PAY-1", 0.0, PaymentMethod.CASH));
        assertThrows(ValidationException.class,
                () -> PaymentFactory.createPayment("PAY-1", 100.0, null));
        assertThrows(ValidationException.class,
                () -> PaymentFactory.createPayment("PAY-1", 100.0, PaymentMethod.CASH, null));
    }

    @Test
    void createsConcretePaymentStrategiesWithExpectedMethodsAndValidation() {
        CardPayment card = assertInstanceOf(CardPayment.class,
                PaymentFactory.createCardPayment(" 4111-1111 ", " Nguyen Van A "));
        assertEquals("4111-1111", card.getCardNumber());
        assertEquals("Nguyen Van A", card.getCardHolderName());
        assertEquals(PaymentMethod.CREDIT_DEBIT_CARD, card.getPaymentMethod());

        EWalletPayment wallet = assertInstanceOf(EWalletPayment.class,
                PaymentFactory.createEWalletPayment(" W-01 ", " MoMo "));
        assertEquals("W-01", wallet.getWalletId());
        assertEquals("MoMo", wallet.getProvider());
        assertEquals(PaymentMethod.E_WALLET, wallet.getPaymentMethod());

        BankTransferPayment transfer = assertInstanceOf(BankTransferPayment.class,
                PaymentFactory.createBankTransferPayment(" Vietcombank ", " 0123456789 "));
        assertEquals("Vietcombank", transfer.getBankName());
        assertEquals("0123456789", transfer.getAccountNumber());
        assertEquals(PaymentMethod.BANK_TRANSFER, transfer.getPaymentMethod());

        CashPayment cash = assertInstanceOf(CashPayment.class,
                PaymentFactory.createCashPayment(" CNT-01 ", " ST-01 "));
        assertEquals("CNT-01", cash.getCounterCode());
        assertEquals("ST-01", cash.getCashierId());
        assertEquals(PaymentMethod.CASH, cash.getPaymentMethod());

        assertThrows(ValidationException.class, () -> PaymentFactory.createCardPayment(" ", "Alice"));
        assertThrows(ValidationException.class, () -> PaymentFactory.createEWalletPayment("W-01", null));
        assertThrows(ValidationException.class, () -> PaymentFactory.createBankTransferPayment(null, "123"));
        assertThrows(ValidationException.class, () -> PaymentFactory.createCashPayment("CNT-01", " "));
    }
}
