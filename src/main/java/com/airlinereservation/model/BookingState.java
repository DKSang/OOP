package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;

/**
 * State contract encapsulating booking lifecycle transitions and flight-event reactions.
 */
public interface BookingState {

    BookingStatus getStatus();

    default void validateConfirmable(Booking booking) {
        throw booking.invalidTransition("confirm");
    }

    default void confirmBooking(Booking booking, Payment payment) {
        throw booking.invalidTransition("confirm");
    }

    default void checkIn(Booking booking) {
        throw booking.invalidTransition("check in");
    }

    default boolean cancelBooking(Booking booking) {
        throw booking.invalidTransition("cancel");
    }

    default void complete(Booking booking) {
        throw booking.invalidTransition("complete");
    }

    default void cancelPendingOnArrival(Booking booking) {
        throw booking.invalidTransition("expire");
    }

    default void onFlightCancelled(Booking booking) {
        throw booking.invalidTransition("cancel on flight cancellation");
    }

    default void onFlightArrived(Booking booking) {
        // Terminal or non-transitioning states remain unchanged unless overridden.
    }
}
