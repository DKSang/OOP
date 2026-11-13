package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;

/**
 * Initial unpaid booking state awaiting payment confirmation or cancellation.
 */
public final class PendingBookingState implements BookingState {

    static final PendingBookingState INSTANCE = new PendingBookingState();

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.PENDING;
    }

    @Override
    public void validateConfirmable(Booking booking) {
        booking.requireActiveFlight("confirm");
        booking.requireReservedSeats("confirm");
    }

    @Override
    public void confirmBooking(Booking booking, Payment payment) {
        validateConfirmable(booking);
        booking.verifyConfirmablePayment(payment);
        booking.occupySeats();
        booking.attachPayment(payment);
        booking.transitionTo(ConfirmedBookingState.INSTANCE);
    }

    @Override
    public boolean cancelBooking(Booking booking) {
        booking.requireCancellableFlight("cancel");
        booking.completeCancellation();
        return true;
    }

    @Override
    public void cancelPendingOnArrival(Booking booking) {
        booking.requireArrivedFlight("expire pending");
        booking.requireHeldSeats("expire");
        booking.releaseSeats();
        booking.transitionTo(CancelledBookingState.INSTANCE);
    }

    @Override
    public void onFlightCancelled(Booking booking) {
        booking.cancelOnFlightCancellation();
    }

    @Override
    public void onFlightArrived(Booking booking) {
        cancelPendingOnArrival(booking);
    }
}
