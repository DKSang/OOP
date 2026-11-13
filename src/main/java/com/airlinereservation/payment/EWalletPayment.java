package com.airlinereservation.payment;

import com.airlinereservation.exception.ValidationException;

/**
 * Payment strategy processing electronic wallet transactions.
 */
public class EWalletPayment implements PaymentStrategy {

    private final String walletId;
    private final String provider;

    public EWalletPayment(String walletId, String provider) {
        this.walletId = ValidationException.requireNonBlank(walletId, "Wallet ID");
        this.provider = ValidationException.requireNonBlank(provider, "Wallet provider");
    }

    public String getWalletId() {
        return walletId;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean pay(double amount) {
        return PaymentStrategy.isPayableAmount(amount);
    }
}
