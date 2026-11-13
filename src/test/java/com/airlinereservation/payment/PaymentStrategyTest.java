package com.airlinereservation.payment;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentStrategyTest {

    @Test
    void definesExpectedPaymentEnumsAndFunctionalStrategySeam() {
        assertEquals(List.of(
                PaymentMethod.CREDIT_DEBIT_CARD,
                PaymentMethod.E_WALLET,
                PaymentMethod.BANK_TRANSFER,
                PaymentMethod.CASH
        ), List.of(PaymentMethod.values()));

        assertEquals(List.of(
                PaymentStatus.PENDING,
                PaymentStatus.COMPLETED,
                PaymentStatus.FAILED,
                PaymentStatus.REFUNDED
        ), List.of(PaymentStatus.values()));

        PaymentStrategy alwaysApprove = amount -> true;
        PaymentStrategy alwaysDecline = amount -> false;
        assertTrue(alwaysApprove.pay(1_000_000));
        assertFalse(alwaysDecline.pay(1_000_000));
    }

    @Test
    void concreteStrategiesValidateInputsAndEvaluatePositiveAmounts() {
        CardPayment card = new CardPayment(" 4111-1111-1111-1111 ", " Nguyen Van A ");
        assertEquals("4111-1111-1111-1111", card.getCardNumber());
        assertEquals("Nguyen Van A", card.getCardHolderName());

        EWalletPayment wallet = new EWalletPayment(" W-001 ", " MoMo ");
        assertEquals("W-001", wallet.getWalletId());
        assertEquals("MoMo", wallet.getProvider());

        BankTransferPayment transfer = new BankTransferPayment(" Vietcombank ", " 0123456789 ");
        assertEquals("Vietcombank", transfer.getBankName());
        assertEquals("0123456789", transfer.getAccountNumber());

        CashPayment cash = new CashPayment(" CNT-01 ", " ST-09 ");
        assertEquals("CNT-01", cash.getCounterCode());
        assertEquals("ST-09", cash.getCashierId());

        for (PaymentStrategy strategy : List.of(card, wallet, transfer, cash)) {
            assertTrue(strategy.pay(500_000));
            assertFalse(strategy.pay(0));
            assertFalse(strategy.pay(-1));
            assertFalse(strategy.pay(Double.NaN));
            assertFalse(strategy.pay(Double.POSITIVE_INFINITY));
        }

        assertThrows(ValidationException.class, () -> new CardPayment(null, "Alice"));
        assertThrows(ValidationException.class, () -> new CardPayment("4111", "  "));
        assertThrows(ValidationException.class, () -> new EWalletPayment("", "MoMo"));
        assertThrows(ValidationException.class, () -> new EWalletPayment("W-1", null));
        assertThrows(ValidationException.class, () -> new BankTransferPayment(" ", "123"));
        assertThrows(ValidationException.class, () -> new BankTransferPayment("VCB", null));
        assertThrows(ValidationException.class, () -> new CashPayment(null, "ST-01"));
        assertThrows(ValidationException.class, () -> new CashPayment("C-01", " "));
    }
}
