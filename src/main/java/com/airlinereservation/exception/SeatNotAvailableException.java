package com.airlinereservation.exception;

/**
 * Thrown when a requested seat is unavailable for booking.
 */
public class SeatNotAvailableException extends AirlineReservationException {

    public SeatNotAvailableException(String message) {
        super(message);
    }

    public SeatNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
