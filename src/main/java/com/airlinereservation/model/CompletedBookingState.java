package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;

/**
 * Terminal state reached after a checked-in booking completes on flight arrival.
 */
public final class CompletedBookingState implements BookingState {

    static final CompletedBookingState INSTANCE = new CompletedBookingState();

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.COMPLETED;
    }
}
