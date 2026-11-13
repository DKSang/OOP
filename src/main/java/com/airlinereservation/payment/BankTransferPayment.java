package com.airlinereservation.payment;

import com.airlinereservation.exception.ValidationException;

/**
 * Payment strategy processing bank transfer transactions.
 */
public class BankTransferPayment implements PaymentStrategy {

    private final String bankName;
    private final String accountNumber;

    public BankTransferPayment(String bankName, String accountNumber) {
        this.bankName = ValidationException.requireNonBlank(bankName, "Bank name");
        this.accountNumber = ValidationException.requireNonBlank(accountNumber, "Account number");
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public boolean pay(double amount) {
        return PaymentStrategy.isPayableAmount(amount);
    }
}
