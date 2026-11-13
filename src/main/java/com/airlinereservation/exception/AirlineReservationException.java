package com.airlinereservation.exception;

/**
 * Base unchecked domain exception for all Airline Reservation System errors.
 */
public class AirlineReservationException extends RuntimeException {

    public AirlineReservationException(String message) {
        super(message);
    }

    public AirlineReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
