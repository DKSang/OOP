package com.airlinereservation.exception;

/**
 * Thrown when registering an entity with an identifier that already exists.
 */
public class DuplicateEntityException extends AirlineReservationException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
