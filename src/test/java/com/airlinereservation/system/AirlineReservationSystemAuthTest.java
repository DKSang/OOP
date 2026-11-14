package com.airlinereservation.system;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirlineReservationSystemAuthTest {

    @Test
    void registersFindsAndListsAdminsImmutably() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Admin a1 = new Admin("ADM-01", "Le Minh Cuong", "cuong@airline.vn", "0909111111", "STF-01", "Operations", "admin123");
        Admin a2 = new Admin("ADM-02", "Pham Thu Ha", "ha@airline.vn", "0909222222", "STF-02", "Support");

        system.registerAdmin(a1);
        system.registerAdmin(a2);

        assertSame(a1, system.findAdminById("ADM-01"));
        assertSame(a2, system.findAdminById(" ADM-02 "));
        assertEquals(List.of(a1, a2), system.getAdmins());
        assertThrows(UnsupportedOperationException.class, () -> system.getAdmins().add(a1));

        assertThrows(ValidationException.class, () -> system.registerAdmin(null));
        assertThrows(DuplicateEntityException.class, () -> system.registerAdmin(
                new Admin("ADM-01", "Other", "other@airline.vn", "0909333333", "STF-99", "Ops")));
        assertThrows(DuplicateEntityException.class, () -> system.registerAdmin(
                new Admin("ADM-03", "Other", "other@airline.vn", "0909333333", "stf-01", "Ops")));
        assertThrows(EntityNotFoundException.class, () -> system.findAdminById("ADM-404"));
    }

    @Test
    void authenticatesAdminsAndPassengersBySupportedIdentifiersAndRejectsInvalidCredentials() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        Admin admin = new Admin("ADM-01", "Le Minh Cuong", "admin@airline.vn", "0909111111", "STF-001", "Operations", "admin123");
        Passenger passenger = new Passenger("PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001", "paxPass");
        system.registerAdmin(admin);
        system.registerPassenger(passenger);

        Person byAdminId = system.authenticate("ADM-01", "admin123");
        Person byStaffCode = system.authenticate("stf-001", "admin123");
        Person byAdminEmail = system.authenticate("ADMIN@AIRLINE.VN", "admin123");
        assertSame(admin, byAdminId);
        assertSame(admin, byStaffCode);
        assertSame(admin, byAdminEmail);

        Person byPassengerId = system.authenticate("PAX-01", "paxPass");
        Person byPassengerEmail = system.authenticate("a@example.com", "paxPass");
        Person byPassport = system.authenticate("cccd-001", "paxPass");
        assertSame(passenger, byPassengerId);
        assertSame(passenger, byPassengerEmail);
        assertSame(passenger, byPassport);

        Passenger spacePasswordPassenger = new Passenger("PAX-02", "Space Pass", "shared@example.com", "0902222222",
                LocalDate.of(2000, 2, 2), "Female", "CCCD-002", " keepSpaces ");
        system.registerPassenger(spacePasswordPassenger);
        assertSame(spacePasswordPassenger, system.authenticate("PAX-02", " keepSpaces "));
        assertThrows(ValidationException.class, () -> system.authenticate("PAX-02", "keepSpaces"));

        assertThrows(ValidationException.class, () -> system.authenticate("ADM-01", "wrong"));
        assertThrows(ValidationException.class, () -> system.authenticate("PAX-01", "wrong"));
        assertThrows(ValidationException.class, () -> system.authenticate("UNKNOWN", "123456"));
        assertThrows(ValidationException.class, () -> system.authenticate("  ", "123456"));
        assertThrows(ValidationException.class, () -> system.authenticate("PAX-01", "  "));
    }

    @Test
    void clearsAllCatalogsAndRegistries() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        system.addAirport(new Airport("HAN", "Noi Bai", "Hanoi"));
        system.addAircraft(new Aircraft("AC-320", "A320neo", "Airbus", 10));
        system.registerAdmin(new Admin("ADM-01", "Admin", "admin@airline.vn", "0909", "STF-01", "Ops"));
        system.registerPassenger(new Passenger("PAX-01", "Passenger", "p@example.com", "0901",
                LocalDate.of(2000, 1, 1), "Male", "CCCD-01"));

        system.clear();

        assertTrue(system.getAirports().isEmpty());
        assertTrue(system.getAircrafts().isEmpty());
        assertTrue(system.getFlights().isEmpty());
        assertTrue(system.getAdmins().isEmpty());
        assertTrue(system.getPassengers().isEmpty());
        assertTrue(system.getBookings().isEmpty());
    }
}
