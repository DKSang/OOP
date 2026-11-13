package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;

/**
 * Terminal state for a cancelled booking whose seats have been released.
 */
public final class CancelledBookingState implements BookingState {

    static final CancelledBookingState INSTANCE = new CancelledBookingState();

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }

    @Override
    public void onFlightCancelled(Booking booking) {
        // Already cancelled; flight cancellation cascades remain idempotent.
    }
}
