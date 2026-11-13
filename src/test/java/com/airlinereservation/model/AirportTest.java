package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirportTest {

    @Test
    void createsAirportAndTrimsFields() {
        Airport airport = new Airport(" HAN ", " Noi Bai International ", " Hanoi ");

        assertEquals("HAN", airport.getAirportCode());
        assertEquals("Noi Bai International", airport.getAirportName());
        assertEquals("Hanoi", airport.getCity());
    }

    @Test
    void rejectsNullOrBlankAirportFields() {
        assertThrows(ValidationException.class, () -> new Airport(null, "Noi Bai", "Hanoi"));
        assertThrows(ValidationException.class, () -> new Airport("   ", "Noi Bai", "Hanoi"));
        assertThrows(ValidationException.class, () -> new Airport("HAN", null, "Hanoi"));
        assertThrows(ValidationException.class, () -> new Airport("HAN", "  ", "Hanoi"));
        assertThrows(ValidationException.class, () -> new Airport("HAN", "Noi Bai", null));
        assertThrows(ValidationException.class, () -> new Airport("HAN", "Noi Bai", "\t"));
    }
}
