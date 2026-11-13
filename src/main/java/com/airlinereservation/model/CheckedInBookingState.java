package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;

/**
 * Checked-in booking state awaiting flight arrival completion or flight cancellation.
 */
public final class CheckedInBookingState implements BookingState {

    static final CheckedInBookingState INSTANCE = new CheckedInBookingState();

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CHECKED_IN;
    }

    @Override
    public void complete(Booking booking) {
        booking.requireArrivedFlight("complete");
        booking.transitionTo(CompletedBookingState.INSTANCE);
    }

    @Override
    public void onFlightCancelled(Booking booking) {
        booking.cancelOnFlightCancellation();
    }

    @Override
    public void onFlightArrived(Booking booking) {
        complete(booking);
    }
}
