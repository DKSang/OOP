package com.airlinereservation.exception;

/**
 * Thrown when an operation is invalid for an entity's current lifecycle state.
 */
public class InvalidStateException extends AirlineReservationException {

    public InvalidStateException(String message) {
        super(message);
    }

    public InvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
