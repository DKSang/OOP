package com.airlinereservation.exception;

/**
 * Thrown when payment processing or refunding encounters a domain error.
 */
public class PaymentException extends AirlineReservationException {

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
