package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

/**
 * Business cabin seat applying a fare multiplier and optional lounge access.
 */
public class BusinessSeat extends Seat {

    private static final double DEFAULT_PRICE_MULTIPLIER = 2.0;

    private final double priceMultiplier;
    private final boolean hasLoungeAccess;

    public BusinessSeat(String seatNumber) {
        this(seatNumber, DEFAULT_PRICE_MULTIPLIER, true);
    }

    public BusinessSeat(String seatNumber, double priceMultiplier, boolean hasLoungeAccess) {
        super(seatNumber);
        if (!Double.isFinite(priceMultiplier) || priceMultiplier <= 1.0) {
            throw new ValidationException("Business seat price multiplier must be greater than 1.0");
        }
        this.priceMultiplier = priceMultiplier;
        this.hasLoungeAccess = hasLoungeAccess;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public boolean hasLoungeAccess() {
        return hasLoungeAccess;
    }

    @Override
    public double calculatePrice(double basePrice) {
        return requireValidBasePrice(basePrice) * priceMultiplier;
    }

    @Override
    public String getSeatType() {
        return "Business";
    }
}
