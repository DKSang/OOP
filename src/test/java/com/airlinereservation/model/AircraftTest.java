package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AircraftTest {

    @Test
    void createsAircraftAndExposesCapacityAndModelInfo() {
        Aircraft aircraft = new Aircraft(" AC-001 ", " A321neo ", " Airbus ", 180);

        assertEquals("AC-001", aircraft.getAircraftId());
        assertEquals("A321neo", aircraft.getModel());
        assertEquals("Airbus", aircraft.getManufacturer());
        assertEquals(180, aircraft.getCapacity());
        assertEquals("Airbus A321neo (180 seats)", aircraft.getModelInfo());
    }

    @Test
    void rejectsInvalidAircraftFieldsAndNonPositiveCapacity() {
        assertThrows(ValidationException.class, () -> new Aircraft(null, "A321", "Airbus", 180));
        assertThrows(ValidationException.class, () -> new Aircraft("  ", "A321", "Airbus", 180));
        assertThrows(ValidationException.class, () -> new Aircraft("AC-001", null, "Airbus", 180));
        assertThrows(ValidationException.class, () -> new Aircraft("AC-001", " ", "Airbus", 180));
        assertThrows(ValidationException.class, () -> new Aircraft("AC-001", "A321", null, 180));
        assertThrows(ValidationException.class, () -> new Aircraft("AC-001", "A321", " ", 180));
        assertThrows(ValidationException.class, () -> new Aircraft("AC-001", "A321", "Airbus", 0));
        assertThrows(ValidationException.class, () -> new Aircraft("AC-001", "A321", "Airbus", -12));
    }
}
