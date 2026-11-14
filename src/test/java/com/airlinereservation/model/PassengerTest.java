package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassengerTest {

    @Test
    void createsPassengerUpdatesProfileAndRejectsInvalidFields() {
        LocalDate dob = LocalDate.of(2000, 5, 14);
        Passenger passenger = new Passenger(
                " PAX-01 ",
                " Nguyen Van B ",
                " b.nguyen@example.com ",
                " 0912345678 ",
                dob,
                " Male ",
                " 079200001234 "
        );

        assertEquals("PAX-01", passenger.getId());
        assertEquals("Nguyen Van B", passenger.getFullName());
        assertEquals("b.nguyen@example.com", passenger.getEmail());
        assertEquals("0912345678", passenger.getPhoneNumber());
        assertEquals(dob, passenger.getDateOfBirth());
        assertEquals("Male", passenger.getGender());
        assertEquals("079200001234", passenger.getPassportOrId());
        assertEquals("Passenger", passenger.getRole());

        passenger.updateProfile(" Nguyen Van B Updated ", " P99887766 ", " 0999888777 ", " updated@example.com ");
        assertEquals("Nguyen Van B Updated", passenger.getFullName());
        assertEquals("P99887766", passenger.getPassportOrId());
        assertEquals("0999888777", passenger.getPhoneNumber());
        assertEquals("updated@example.com", passenger.getEmail());

        assertThrows(ValidationException.class, () -> passenger.updateProfile(" ", "P1", "090", "e@x.com"));
        assertEquals("Nguyen Van B Updated", passenger.getFullName());

        assertThrows(ValidationException.class, () -> new Passenger("PAX-02", "Name", "e@x.com", "090", null, "Male", "ID1"));
        assertThrows(ValidationException.class, () -> new Passenger("PAX-02", "Name", "e@x.com", "090", dob, " ", "ID1"));
        assertThrows(ValidationException.class, () -> new Passenger("PAX-02", "Name", "e@x.com", "090", dob, "Male", null));
    }

    @Test
    void recordsBookingsAndReturnsImmutableBookingHistory() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 4);
        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000
        );
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van B", "b.nguyen@example.com", "0912345678",
                LocalDate.of(2000, 5, 14), "Male", "079200001234"
        );
        Passenger otherPassenger = new Passenger(
                "PAX-02", "Tran Thi C", "c@example.com", "0912345679",
                LocalDate.of(2001, 6, 15), "Female", "079200005678"
        );

        Booking booking = new Booking(
                "BK-01", flight, passenger, List.of(flight.findSeat("1A")),
                LocalDateTime.of(2026, 11, 13, 12, 0)
        );
        Booking otherBooking = new Booking(
                "BK-02", flight, otherPassenger, List.of(flight.findSeat("1B")),
                LocalDateTime.of(2026, 11, 13, 12, 5)
        );

        passenger.addBooking(booking);

        assertEquals(List.of(booking), passenger.getBookingHistory());
        assertThrows(UnsupportedOperationException.class, () -> passenger.getBookingHistory().add(booking));
        assertThrows(ValidationException.class, () -> passenger.addBooking(null));
        assertThrows(ValidationException.class, () -> passenger.addBooking(otherBooking));
    }

    @Test
    void supportsDefaultAndExplicitPasswordAuthenticationAndChangePassword() {
        Passenger defaultPasswordPassenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        assertTrue(defaultPasswordPassenger.authenticate("123456"));
        assertFalse(defaultPasswordPassenger.authenticate("wrong"));
        assertFalse(defaultPasswordPassenger.authenticate(null));

        Passenger customPasswordPassenger = new Passenger(
                "PAX-02", "Tran Thi B", "b@example.com", "0902222222",
                LocalDate.of(1999, 5, 20), "Female", "CCCD-002", "secretPass"
        );
        assertTrue(customPasswordPassenger.authenticate("secretPass"));
        assertFalse(customPasswordPassenger.authenticate("123456"));

        customPasswordPassenger.changePassword("secretPass", "newPass123");
        assertTrue(customPasswordPassenger.authenticate("newPass123"));
        assertFalse(customPasswordPassenger.authenticate("secretPass"));

        assertThrows(ValidationException.class,
                () -> customPasswordPassenger.changePassword("wrongOld", "anotherPass"));
        assertThrows(ValidationException.class,
                () -> customPasswordPassenger.changePassword("newPass123", "  "));
        assertThrows(ValidationException.class, () -> new Passenger(
                "PAX-03", "Le Van C", "c@example.com", "0903333333",
                LocalDate.of(2000, 1, 1), "Male", "CCCD-003", "  "
        ));
    }
}
