package com.airlinereservation.payment;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.model.Payment;

import java.time.Clock;

/**
 * Factory for creating {@link Payment} transaction records and concrete {@link PaymentStrategy} adapters.
 */
public final class PaymentFactory {

    private PaymentFactory() {
    }

    public static Payment createPayment(String paymentId, double amount, PaymentMethod method) {
        return new Payment(paymentId, amount, method);
    }

    public static Payment createPayment(String paymentId, double amount, PaymentMethod method, Clock clock) {
        return new Payment(paymentId, amount, method, clock);
    }

    public static CardPayment createCardPayment(String cardNumber, String cardHolderName) {
        return new CardPayment(cardNumber, cardHolderName);
    }

    public static EWalletPayment createEWalletPayment(String walletId, String provider) {
        return new EWalletPayment(walletId, provider);
    }

    public static BankTransferPayment createBankTransferPayment(String bankName, String accountNumber) {
        return new BankTransferPayment(bankName, accountNumber);
    }

    public static CashPayment createCashPayment(String counterCode, String cashierId) {
        return new CashPayment(counterCode, cashierId);
    }
}
