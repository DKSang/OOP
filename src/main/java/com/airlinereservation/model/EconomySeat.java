package com.airlinereservation.model;

/**
 * Economy cabin seat with optional extra-legroom surcharge.
 */
public class EconomySeat extends Seat {

    private static final double EXTRA_LEGROOM_MULTIPLIER = 1.15;

    private final boolean hasExtraLegroom;

    public EconomySeat(String seatNumber, boolean hasExtraLegroom) {
        super(seatNumber);
        this.hasExtraLegroom = hasExtraLegroom;
    }

    public boolean hasExtraLegroom() {
        return hasExtraLegroom;
    }

    @Override
    public double calculatePrice(double basePrice) {
        double validBasePrice = requireValidBasePrice(basePrice);
        return hasExtraLegroom ? validBasePrice * EXTRA_LEGROOM_MULTIPLIER : validBasePrice;
    }

    @Override
    public String getSeatType() {
        return "Economy";
    }
}
