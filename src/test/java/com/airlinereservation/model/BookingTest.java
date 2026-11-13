package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingTest {

    @Test
    void createsPendingBookingCalculatesPolymorphicTotalAndProtectsSeatList() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Seat businessSeat = flight.findSeat("1A"); // 2.0 * 1_000_000 = 2_000_000
        Seat extraLegroomEconomy = flight.findSeat("2A"); // 1.15 * 1_000_000 = 1_150_000
        Seat standardEconomy = flight.findSeat("3A"); // 1.0 * 1_000_000 = 1_000_000
        List<Seat> mutableInput = new ArrayList<>(List.of(businessSeat, extraLegroomEconomy, standardEconomy));
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 13, 10, 30);

        Booking booking = new Booking(" BK-01 ", flight, passenger, mutableInput, createdAt);
        mutableInput.clear();

        assertEquals("BK-01", booking.getBookingId());
        assertSame(flight, booking.getFlight());
        assertSame(passenger, booking.getPassenger());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(createdAt, booking.getCreatedAt());
        assertEquals(4_150_000.0, booking.calculateTotalPrice(), 0.001);
        assertEquals(4_150_000.0, booking.getTotalPrice(), 0.001);
        assertEquals(List.of(businessSeat, extraLegroomEconomy, standardEconomy), booking.getSelectedSeats());
        assertEquals(List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.COMPLETED,
                BookingStatus.CANCELLED
        ), List.of(BookingStatus.values()));

        assertThrows(UnsupportedOperationException.class, () -> booking.getSelectedSeats().add(businessSeat));
    }

    @Test
    void rejectsInvalidBookingArguments() {
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
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Seat seat = flight.findSeat("1A");
        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);

        assertThrows(ValidationException.class, () -> new Booking(" ", flight, passenger, List.of(seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", null, passenger, List.of(seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, null, List.of(seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, null, now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, List.of(), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, List.of(seat, seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, List.of(seat), null));
    }
}
