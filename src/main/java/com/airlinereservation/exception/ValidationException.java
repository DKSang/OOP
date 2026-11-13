package com.airlinereservation.exception;

/**
 * Thrown when input arguments or domain invariants fail validation.
 */
public class ValidationException extends AirlineReservationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
        return value;
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static double requirePositive(double value, String fieldName) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
        return value;
    }
}
