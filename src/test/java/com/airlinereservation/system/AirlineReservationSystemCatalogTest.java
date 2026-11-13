package com.airlinereservation.system;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirlineReservationSystemCatalogTest {

    @Test
    void registersAndFindsAirportsCaseInsensitivelyInInsertionOrder() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");

        system.addAirport(han);
        system.addAirport(sgn);

        assertSame(han, system.findAirportByCode("han"));
        assertSame(sgn, system.findAirportByCode(" sGn "));
        assertEquals(List.of(han, sgn), system.getAirports());
    }

    @Test
    void rejectsNullDuplicateOrMissingAirportsAndReturnsImmutableAirportList() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        system.addAirport(han);

        assertThrows(ValidationException.class, () -> system.addAirport(null));
        assertThrows(DuplicateEntityException.class,
                () -> system.addAirport(new Airport("han", "Duplicate Noi Bai", "Hanoi")));
        assertThrows(ValidationException.class, () -> system.findAirportByCode(null));
        assertThrows(ValidationException.class, () -> system.findAirportByCode("  "));
        assertThrows(EntityNotFoundException.class, () -> system.findAirportByCode("DAD"));

        List<Airport> airports = system.getAirports();
        assertThrows(UnsupportedOperationException.class,
                () -> airports.add(new Airport("DAD", "Da Nang", "Da Nang")));
    }

    @Test
    void registersAndFindsAircraftsInInsertionOrder() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Aircraft a321 = new Aircraft("AC-01", "A321neo", "Airbus", 180);
        Aircraft b787 = new Aircraft("AC-02", "787-9", "Boeing", 280);

        system.addAircraft(a321);
        system.addAircraft(b787);

        assertSame(a321, system.findAircraftById("AC-01"));
        assertSame(b787, system.findAircraftById(" AC-02 "));
        assertEquals(List.of(a321, b787), system.getAircrafts());
    }

    @Test
    void rejectsNullDuplicateOrMissingAircraftsAndReturnsImmutableAircraftList() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Aircraft a321 = new Aircraft("AC-01", "A321neo", "Airbus", 180);
        system.addAircraft(a321);

        assertThrows(ValidationException.class, () -> system.addAircraft(null));
        assertThrows(DuplicateEntityException.class,
                () -> system.addAircraft(new Aircraft("AC-01", "A321ceo", "Airbus", 184)));
        assertThrows(ValidationException.class, () -> system.findAircraftById(null));
        assertThrows(ValidationException.class, () -> system.findAircraftById("  "));
        assertThrows(EntityNotFoundException.class, () -> system.findAircraftById("AC-99"));

        List<Aircraft> aircrafts = system.getAircrafts();
        assertThrows(UnsupportedOperationException.class,
                () -> aircrafts.add(new Aircraft("AC-03", "A350", "Airbus", 300)));
    }
}
