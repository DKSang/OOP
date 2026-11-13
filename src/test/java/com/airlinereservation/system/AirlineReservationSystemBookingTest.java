package com.airlinereservation.system;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.EconomySeat;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirlineReservationSystemBookingTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-11-13T15:00:00Z");

    private AirlineReservationSystem system;
    private Airport han;
    private Airport sgn;
    private Aircraft a320;
    private Flight flight1;
    private Flight flight2;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        system = new AirlineReservationSystem(Clock.fixed(FIXED_INSTANT, ZONE));
        han = new Airport("HAN", "Noi Bai", "Hanoi");
        sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);

        system.addAirport(han);
        system.addAirport(sgn);
        system.addAircraft(a320);

        flight1 = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        flight2 = new Flight(
                "FL-02", "VN103", han, sgn,
                LocalDateTime.of(2026, 12, 10, 13, 0),
                LocalDateTime.of(2026, 12, 10, 15, 15),
                a320, 1_000_000, 4
        );
        system.addFlight(flight1);
        system.addFlight(flight2);

        passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        system.registerPassenger(passenger);
    }

    @Test
    void createsPendingBookingWithClockReservesSeatsAndUpdatesPassengerHistoryImmutably() {
        Seat seat1A = flight1.findSeat("1A"); // Business: 2_000_000
        Seat seat2A = flight1.findSeat("2A"); // Extra-legroom Economy: 1_150_000

        Booking booking = system.createBooking(passenger, flight1, List.of(seat1A, seat2A));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(LocalDateTime.ofInstant(FIXED_INSTANT, ZONE), booking.getCreatedAt());
        assertEquals(3_150_000.0, booking.getTotalPrice(), 0.001);
        assertEquals(SeatStatus.RESERVED, seat1A.getStatus());
        assertEquals(SeatStatus.RESERVED, seat2A.getStatus());
        assertTrue(flight2.findSeat("1A").isAvailable());

        assertSame(booking, system.findBookingById(booking.getBookingId()));
        assertEquals(List.of(booking), system.getBookings());
        assertEquals(List.of(booking), passenger.getBookingHistory());
        assertEquals(List.of(seat1A, seat2A), booking.getSelectedSeats());

        assertThrows(UnsupportedOperationException.class, () -> system.getBookings().add(booking));
        assertThrows(UnsupportedOperationException.class, () -> passenger.getBookingHistory().add(booking));
        assertThrows(UnsupportedOperationException.class, () -> booking.getSelectedSeats().add(seat1A));

        Booking byIdsBooking = system.createBooking("PAX-01", "FL-02", List.of("3A"));
        assertEquals(BookingStatus.PENDING, byIdsBooking.getStatus());
        assertEquals(1_000_000.0, byIdsBooking.getTotalPrice(), 0.001);
        assertEquals(SeatStatus.RESERVED, flight2.findSeat("3A").getStatus());
        assertEquals(List.of(booking, byIdsBooking), passenger.getBookingHistory());
    }

    @Test
    void rejectsDuplicateForeignAlreadyReservedSeatsAndInvalidBookingConditionsAtomically() {
        assertThrows(ValidationException.class, () -> new AirlineReservationSystem(null));

        Seat flight1Seat1A = flight1.findSeat("1A");
        Seat flight1Seat1B = flight1.findSeat("1B");
        Seat foreignSeat = flight2.findSeat("1A");
        Seat detachedSeat = new EconomySeat("1A", false);

        // Duplicate seat in request -> ValidationException, seat remains available
        assertThrows(ValidationException.class,
                () -> system.createBooking(passenger, flight1, List.of(flight1Seat1A, flight1Seat1A)));
        assertThrows(ValidationException.class,
                () -> system.createBooking("PAX-01", "FL-01", List.of("1a", "1A")));
        assertThrows(ValidationException.class,
                () -> system.createBooking(passenger, flight1, List.of()));
        assertTrue(flight1Seat1A.isAvailable());

        // Foreign seat mixed with available seat -> SeatNotAvailableException, no partial reservation
        assertThrows(SeatNotAvailableException.class,
                () -> system.createBooking(passenger, flight1, List.of(flight1Seat1A, foreignSeat)));
        assertThrows(SeatNotAvailableException.class,
                () -> system.createBooking(passenger, flight1, List.of(flight1Seat1A, detachedSeat)));
        assertTrue(flight1Seat1A.isAvailable());
        assertTrue(foreignSeat.isAvailable());

        // Already reserved seat mixed with available seat -> SeatNotAvailableException, other seat stays available
        flight1Seat1B.reserve();
        assertThrows(SeatNotAvailableException.class,
                () -> system.createBooking(passenger, flight1, List.of(flight1Seat1A, flight1Seat1B)));
        assertTrue(flight1Seat1A.isAvailable());

        // Unregistered passenger / flight
        Passenger unregisteredPassenger = new Passenger(
                "PAX-99", "Tran Van X", "x@example.com", "0909999999",
                LocalDate.of(1995, 3, 20), "Male", "CCCD-999"
        );
        Flight unregisteredFlight = new Flight(
                "FL-99", "VN999", han, sgn,
                LocalDateTime.of(2026, 12, 11, 7, 0),
                LocalDateTime.of(2026, 12, 11, 9, 15),
                a320, 1_000_000, 4
        );
        assertThrows(EntityNotFoundException.class,
                () -> system.createBooking(unregisteredPassenger, flight1, List.of(flight1Seat1A)));
        assertThrows(EntityNotFoundException.class,
                () -> system.createBooking(passenger, unregisteredFlight, List.of(unregisteredFlight.findSeat("1A"))));

        // Non-SCHEDULED flight -> InvalidStateException
        flight1.updateStatus(FlightStatus.BOARDING);
        assertThrows(InvalidStateException.class,
                () -> system.createBooking(passenger, flight1, List.of(flight1Seat1A)));
        assertTrue(flight1Seat1A.isAvailable());

        // Lookup validation
        assertThrows(ValidationException.class, () -> system.findBookingById(null));
        assertThrows(ValidationException.class, () -> system.findBookingById("  "));
        assertThrows(EntityNotFoundException.class, () -> system.findBookingById("BK-404"));
        assertTrue(passenger.getBookingHistory().isEmpty());
        assertTrue(system.getBookings().isEmpty());
    }
}
