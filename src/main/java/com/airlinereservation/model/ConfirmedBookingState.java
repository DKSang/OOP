package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;

/**
 * Paid booking state eligible for passenger check-in or pre-departure cancellation.
 */
public final class ConfirmedBookingState implements BookingState {

    static final ConfirmedBookingState INSTANCE = new ConfirmedBookingState();

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CONFIRMED;
    }

    @Override
    public void checkIn(Booking booking) {
        booking.requireActiveFlight("check in");
        booking.transitionTo(CheckedInBookingState.INSTANCE);
    }

    @Override
    public boolean cancelBooking(Booking booking) {
        booking.requireCancellableFlight("cancel");
        booking.completeCancellation();
        return true;
    }

    @Override
    public void onFlightCancelled(Booking booking) {
        booking.cancelOnFlightCancellation();
    }

    @Override
    public void onFlightArrived(Booking booking) {
        booking.requireArrivedFlight("process arrival for");
    }
}
