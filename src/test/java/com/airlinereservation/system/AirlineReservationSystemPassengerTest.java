package com.airlinereservation.system;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Passenger;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirlineReservationSystemPassengerTest {

    @Test
    void registersFindsAndListsPassengersInInsertionOrderImmutably() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Passenger p1 = new Passenger("PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001");
        Passenger p2 = new Passenger("PAX-02", "Le Thi B", "b@example.com", "0902222222",
                LocalDate.of(2001, 8, 20), "Female", "CCCD-002");

        system.registerPassenger(p1);
        system.registerPassenger(p2);

        assertSame(p1, system.findPassengerById("PAX-01"));
        assertSame(p2, system.findPassengerById(" PAX-02 "));
        assertEquals(List.of(p1, p2), system.getPassengers());
        assertThrows(UnsupportedOperationException.class, () -> system.getPassengers().add(p1));
    }

    @Test
    void rejectsNullDuplicateIdDuplicatePassportOrMissingPassenger() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Passenger p1 = new Passenger("PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "P123456");
        system.registerPassenger(p1);

        assertThrows(ValidationException.class, () -> system.registerPassenger(null));
        assertThrows(DuplicateEntityException.class, () -> system.registerPassenger(
                new Passenger("PAX-01", "Other Name", "other@example.com", "0903333333",
                        LocalDate.of(1999, 2, 2), "Male", "P999999")));
        assertThrows(DuplicateEntityException.class, () -> system.registerPassenger(
                new Passenger("PAX-02", "Other Name", "other@example.com", "0903333333",
                        LocalDate.of(1999, 2, 2), "Male", "p123456")));

        assertThrows(ValidationException.class, () -> system.findPassengerById(null));
        assertThrows(ValidationException.class, () -> system.findPassengerById("  "));
        assertThrows(EntityNotFoundException.class, () -> system.findPassengerById("PAX-404"));
    }
}
