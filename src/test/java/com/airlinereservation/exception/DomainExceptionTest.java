package com.airlinereservation.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionTest {

    @Test
    void allDomainExceptionsExtendAirlineReservationExceptionAndRuntimeException() {
        List<AirlineReservationException> exceptions = List.of(
                new ValidationException("invalid input"),
                new EntityNotFoundException("not found"),
                new DuplicateEntityException("duplicate"),
                new SeatNotAvailableException("seat taken"),
                new InvalidStateException("invalid state"),
                new PaymentException("payment failed")
        );

        assertEquals(6, exceptions.size());
        for (AirlineReservationException exception : exceptions) {
            assertInstanceOf(RuntimeException.class, exception);
            assertInstanceOf(AirlineReservationException.class, exception);
        }
    }

    @Test
    void domainExceptionsPreserveMessageAndCause() {
        Throwable cause = new IllegalStateException("root cause");
        ValidationException validation = new ValidationException("bad field", cause);
        PaymentException payment = new PaymentException("gateway error", cause);

        assertEquals("bad field", validation.getMessage());
        assertSame(cause, validation.getCause());
        assertEquals("gateway error", payment.getMessage());
        assertSame(cause, payment.getCause());
    }
}
