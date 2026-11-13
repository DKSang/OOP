package com.airlinereservation.exception;

/**
 * Thrown when a requested domain entity cannot be found by its identifier.
 */
public class EntityNotFoundException extends AirlineReservationException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
