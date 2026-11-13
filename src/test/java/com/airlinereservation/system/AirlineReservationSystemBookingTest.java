package com.airlinereservation.system;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.EconomySeat;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Payment;
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

    @Test
    void checksInConfirmedBookingAndRejectsInvalidCheckInRequests() {
        Booking pendingBooking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        assertThrows(InvalidStateException.class, () -> system.checkIn(pendingBooking.getBookingId()));

        Booking scheduledBooking = system.createBooking("PAX-01", "FL-01", List.of("1B"));
        system.processPayment(scheduledBooking.getBookingId(), PaymentMethod.CASH, amount -> true);
        system.checkIn(scheduledBooking.getBookingId());
        assertEquals(BookingStatus.CHECKED_IN, scheduledBooking.getStatus());
        assertThrows(InvalidStateException.class, () -> system.checkIn(scheduledBooking.getBookingId()));

        Booking boardingBooking = system.createBooking("PAX-01", "FL-01", List.of("1C"));
        system.processPayment(boardingBooking.getBookingId(), PaymentMethod.CASH, amount -> true);
        Booking departedBooking = system.createBooking("PAX-01", "FL-01", List.of("1D"));
        system.processPayment(departedBooking.getBookingId(), PaymentMethod.CASH, amount -> true);

        flight1.updateStatus(FlightStatus.BOARDING);
        system.checkIn(boardingBooking.getBookingId());
        assertEquals(BookingStatus.CHECKED_IN, boardingBooking.getStatus());

        flight1.updateStatus(FlightStatus.DEPARTED);
        assertThrows(InvalidStateException.class, () -> system.checkIn(departedBooking.getBookingId()));
        assertEquals(BookingStatus.CONFIRMED, departedBooking.getStatus());

        assertThrows(ValidationException.class, () -> system.checkIn(null));
        assertThrows(ValidationException.class, () -> system.checkIn("  "));
        assertThrows(EntityNotFoundException.class, () -> system.checkIn("BK-404"));
    }

    @Test
    void cancelsPendingAndConfirmedBookingsRefundsPaymentAndAllowsAnotherPassengerToRebookReleasedSeats() {
        Passenger secondPassenger = new Passenger(
                "PAX-02", "Tran Thi B", "b@example.com", "0902222222",
                LocalDate.of(1999, 5, 20), "Female", "CCCD-002"
        );
        system.registerPassenger(secondPassenger);

        Booking pendingBooking = system.createBooking("PAX-01", "FL-01", List.of("1A", "2A"));
        assertTrue(system.cancelBooking(pendingBooking.getBookingId()));
        assertEquals(BookingStatus.CANCELLED, pendingBooking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("1A").getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("2A").getStatus());

        Booking confirmedBooking = system.createBooking("PAX-01", "FL-01", List.of("1B", "2B"));
        Payment payment = system.processPayment(confirmedBooking.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD, amount -> true);
        assertTrue(system.cancelBooking(confirmedBooking.getBookingId()));
        assertEquals(BookingStatus.CANCELLED, confirmedBooking.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("1B").getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("2B").getStatus());

        Booking rebooked = system.createBooking("PAX-02", "FL-01", List.of("1A", "1B", "2A", "2B"));
        Payment rebookedPayment = system.processPayment(rebooked.getBookingId(), PaymentMethod.E_WALLET, amount -> true);
        assertEquals(BookingStatus.CONFIRMED, rebooked.getStatus());
        assertEquals(PaymentStatus.COMPLETED, rebookedPayment.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("1A").getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("1B").getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("2A").getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("2B").getStatus());
        assertEquals(List.of(rebooked), secondPassenger.getBookingHistory());
    }

    @Test
    void rejectsCancelBookingForMissingIdCheckedInCancelledOrDepartedArrivedFlight() {
        assertThrows(ValidationException.class, () -> system.cancelBooking(null));
        assertThrows(ValidationException.class, () -> system.cancelBooking("  "));
        assertThrows(EntityNotFoundException.class, () -> system.cancelBooking("BK-404"));

        Booking checkedIn = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        Payment checkedInPayment = system.processPayment(checkedIn.getBookingId(), PaymentMethod.CASH, amount -> true);
        system.checkIn(checkedIn.getBookingId());

        assertThrows(InvalidStateException.class, () -> system.cancelBooking(checkedIn.getBookingId()));
        assertEquals(BookingStatus.CHECKED_IN, checkedIn.getStatus());
        assertEquals(PaymentStatus.COMPLETED, checkedInPayment.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("1A").getStatus());

        Booking cancelled = system.createBooking("PAX-01", "FL-01", List.of("1B"));
        assertTrue(system.cancelBooking(cancelled.getBookingId()));
        assertThrows(InvalidStateException.class, () -> system.cancelBooking(cancelled.getBookingId()));

        Booking pendingOnDeparted = system.createBooking("PAX-01", "FL-02", List.of("1A"));
        Booking confirmedOnDeparted = system.createBooking("PAX-01", "FL-02", List.of("1B"));
        Payment departedPayment = system.processPayment(confirmedOnDeparted.getBookingId(), PaymentMethod.CASH, amount -> true);

        flight2.updateStatus(FlightStatus.BOARDING);
        flight2.updateStatus(FlightStatus.DEPARTED);

        assertThrows(InvalidStateException.class, () -> system.cancelBooking(pendingOnDeparted.getBookingId()));
        assertThrows(InvalidStateException.class, () -> system.cancelBooking(confirmedOnDeparted.getBookingId()));
        assertEquals(BookingStatus.PENDING, pendingOnDeparted.getStatus());
        assertEquals(BookingStatus.CONFIRMED, confirmedOnDeparted.getStatus());
        assertEquals(PaymentStatus.COMPLETED, departedPayment.getStatus());
        assertEquals(SeatStatus.RESERVED, flight2.findSeat("1A").getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight2.findSeat("1B").getStatus());

        flight2.updateStatus(FlightStatus.ARRIVED);
        assertThrows(InvalidStateException.class, () -> system.cancelBooking(pendingOnDeparted.getBookingId()));
        assertThrows(InvalidStateException.class, () -> system.cancelBooking(confirmedOnDeparted.getBookingId()));
    }
}
