package com.airlinereservation.system;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirlineReservationSystemFlightTest {

    private AirlineReservationSystem system;
    private Airport han;
    private Airport sgn;
    private Airport dad;
    private Aircraft a320;

    @BeforeEach
    void setUp() {
        system = new AirlineReservationSystem();
        han = new Airport("HAN", "Noi Bai", "Hanoi");
        sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        dad = new Airport("DAD", "Da Nang", "Da Nang");
        a320 = new Aircraft("AC-320", "A320neo", "Airbus", 4);

        system.addAirport(han);
        system.addAirport(sgn);
        system.addAirport(dad);
        system.addAircraft(a320);
    }

    @Test
    void registersAndFindsFlightsInInsertionOrderWithImmutableList() {
        Flight fl1 = new Flight("FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_500_000);
        Flight fl2 = new Flight("FL-02", "VN102", sgn, han,
                LocalDateTime.of(2026, 12, 10, 13, 0),
                LocalDateTime.of(2026, 12, 10, 15, 15),
                a320, 1_600_000);

        system.addFlight(fl1);
        system.addFlight(fl2);

        assertSame(fl1, system.findFlightById("FL-01"));
        assertSame(fl2, system.findFlightById(" FL-02 "));
        assertEquals(List.of(fl1, fl2), system.getFlights());

        assertThrows(UnsupportedOperationException.class, () -> system.getFlights().add(fl1));
    }

    @Test
    void rejectsNullDuplicateUnregisteredReferencesAndMissingFlightLookup() {
        Flight fl1 = new Flight("FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_500_000);
        system.addFlight(fl1);

        assertThrows(ValidationException.class, () -> system.addFlight(null));
        assertThrows(DuplicateEntityException.class, () -> system.addFlight(
                new Flight("FL-01", "VN999", han, dad,
                        LocalDateTime.of(2026, 12, 11, 8, 0),
                        LocalDateTime.of(2026, 12, 11, 9, 30),
                        a320, 1_200_000)));

        Airport unregisteredAirport = new Airport("CXR", "Cam Ranh", "Khanh Hoa");
        Aircraft unregisteredAircraft = new Aircraft("AC-999", "B737", "Boeing", 120);

        assertThrows(EntityNotFoundException.class, () -> system.addFlight(
                new Flight("FL-BAD-DEP", "VN201", unregisteredAirport, sgn,
                        LocalDateTime.of(2026, 12, 10, 7, 0),
                        LocalDateTime.of(2026, 12, 10, 9, 15),
                        a320, 1_500_000)));
        assertThrows(EntityNotFoundException.class, () -> system.addFlight(
                new Flight("FL-BAD-ARR", "VN202", han, unregisteredAirport,
                        LocalDateTime.of(2026, 12, 10, 7, 0),
                        LocalDateTime.of(2026, 12, 10, 9, 15),
                        a320, 1_500_000)));
        assertThrows(EntityNotFoundException.class, () -> system.addFlight(
                new Flight("FL-BAD-AC", "VN203", han, sgn,
                        LocalDateTime.of(2026, 12, 10, 7, 0),
                        LocalDateTime.of(2026, 12, 10, 9, 15),
                        unregisteredAircraft, 1_500_000)));
        assertThrows(ValidationException.class, () -> system.addFlight(
                new Flight("FL-COLLIDE-AC", "VN204", han, sgn,
                        LocalDateTime.of(2026, 12, 10, 7, 0),
                        LocalDateTime.of(2026, 12, 10, 9, 15),
                        new Aircraft("AC-320", "A320neo", "Airbus", 10), 1_500_000)));

        assertThrows(ValidationException.class, () -> system.findFlightById(null));
        assertThrows(ValidationException.class, () -> system.findFlightById("  "));
        assertThrows(EntityNotFoundException.class, () -> system.findFlightById("FL-404"));
    }

    @Test
    void searchesOnlyScheduledFlightsMatchingRouteDateAndSeatCapacity() {
        LocalDate targetDate = LocalDate.of(2026, 12, 15);
        Flight matchingFull = new Flight("FL-1", "VN101", han, sgn,
                targetDate.atTime(8, 0), targetDate.atTime(10, 15), a320, 1_500_000);
        Flight matchingTight = new Flight("FL-2", "VN103", han, sgn,
                targetDate.atTime(14, 0), targetDate.atTime(16, 15), a320, 1_700_000);
        matchingTight.findSeat("1A").reserve();
        matchingTight.findSeat("1B").reserve();

        Flight boardingFlight = new Flight("FL-3", "VN105", han, sgn,
                targetDate.atTime(18, 0), targetDate.atTime(20, 15), a320, 1_800_000);
        boardingFlight.updateStatus(FlightStatus.BOARDING);

        Flight otherDate = new Flight("FL-4", "VN107", han, sgn,
                targetDate.plusDays(1).atTime(8, 0), targetDate.plusDays(1).atTime(10, 15), a320, 1_500_000);
        Flight otherRoute = new Flight("FL-5", "VN109", han, dad,
                targetDate.atTime(9, 0), targetDate.atTime(10, 30), a320, 1_100_000);

        system.addFlight(matchingFull);
        system.addFlight(matchingTight);
        system.addFlight(boardingFlight);
        system.addFlight(otherDate);
        system.addFlight(otherRoute);

        List<Flight> forTwoSeats = system.searchFlights(" han ", "Sgn", targetDate, 2);
        assertEquals(List.of(matchingFull, matchingTight), forTwoSeats);
        assertThrows(UnsupportedOperationException.class, () -> forTwoSeats.add(otherDate));

        List<Flight> forThreeSeats = system.searchFlights("HAN", "SGN", targetDate, 3);
        assertEquals(List.of(matchingFull), forThreeSeats);

        assertThrows(ValidationException.class, () -> system.searchFlights(null, "SGN", targetDate, 1));
        assertThrows(ValidationException.class, () -> system.searchFlights("HAN", "  ", targetDate, 1));
        assertThrows(ValidationException.class, () -> system.searchFlights("HAN", "SGN", null, 1));
        assertThrows(ValidationException.class, () -> system.searchFlights("HAN", "SGN", targetDate, 0));
        assertThrows(ValidationException.class, () -> system.searchFlights("HAN", "SGN", targetDate, -2));
    }

    @Test
    void cancelsFlightAndCascadesToPendingAndConfirmedBookingsWithoutTouchingOtherFlights() {
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        system.registerPassenger(passenger);

        Flight flight1 = new Flight("FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000);
        Flight flight2 = new Flight("FL-02", "VN102", han, sgn,
                LocalDateTime.of(2026, 12, 10, 13, 0),
                LocalDateTime.of(2026, 12, 10, 15, 15),
                a320, 1_000_000);
        system.addFlight(flight1);
        system.addFlight(flight2);

        Booking pendingBooking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        Booking confirmedBooking = system.createBooking("PAX-01", "FL-01", List.of("1B"));
        Payment confirmedPayment = system.processPayment(confirmedBooking.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD, amount -> true);

        Booking alreadyCancelled = system.createBooking("PAX-01", "FL-01", List.of("1C"));
        system.cancelBooking(alreadyCancelled.getBookingId());

        Booking otherFlightBooking = system.createBooking("PAX-01", "FL-02", List.of("1A"));
        Payment otherPayment = system.processPayment(otherFlightBooking.getBookingId(), PaymentMethod.CASH, amount -> true);

        flight1.updateStatus(FlightStatus.BOARDING);
        system.cancelFlight("FL-01");

        assertEquals(FlightStatus.CANCELLED, flight1.getStatus());
        assertEquals(BookingStatus.CANCELLED, pendingBooking.getStatus());
        assertEquals(BookingStatus.CANCELLED, confirmedBooking.getStatus());
        assertEquals(BookingStatus.CANCELLED, alreadyCancelled.getStatus());
        assertEquals(PaymentStatus.REFUNDED, confirmedPayment.getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("1A").getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("1B").getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("1C").getStatus());

        assertEquals(FlightStatus.SCHEDULED, flight2.getStatus());
        assertEquals(BookingStatus.CONFIRMED, otherFlightBooking.getStatus());
        assertEquals(PaymentStatus.COMPLETED, otherPayment.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight2.findSeat("1A").getStatus());

        assertThrows(InvalidStateException.class, () -> system.cancelFlight("FL-01"));
        assertThrows(ValidationException.class, () -> system.cancelFlight(null));
        assertThrows(ValidationException.class, () -> system.cancelFlight("  "));
        assertThrows(EntityNotFoundException.class, () -> system.cancelFlight("FL-404"));

        flight2.updateStatus(FlightStatus.BOARDING);
        flight2.updateStatus(FlightStatus.DEPARTED);
        assertThrows(InvalidStateException.class, () -> system.cancelFlight("FL-02"));
        assertEquals(FlightStatus.DEPARTED, flight2.getStatus());
        assertEquals(BookingStatus.CONFIRMED, otherFlightBooking.getStatus());
        assertEquals(PaymentStatus.COMPLETED, otherPayment.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight2.findSeat("1A").getStatus());

        flight2.updateStatus(FlightStatus.ARRIVED);
        assertThrows(InvalidStateException.class, () -> system.cancelFlight("FL-02"));
        assertEquals(FlightStatus.ARRIVED, flight2.getStatus());
    }

    @Test
    void arrivesFlightAndCascadesToCheckedInAndPendingBookingsWhilePreservingNoShows() {
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        system.registerPassenger(passenger);

        Aircraft sixSeats = new Aircraft("AC-6", "Regional", "Embraer", 6);
        system.addAircraft(sixSeats);

        Flight flight1 = new Flight("FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                sixSeats, 1_000_000, 2);
        Flight flight2 = new Flight("FL-02", "VN102", han, sgn,
                LocalDateTime.of(2026, 12, 10, 13, 0),
                LocalDateTime.of(2026, 12, 10, 15, 15),
                sixSeats, 1_000_000, 2);
        system.addFlight(flight1);
        system.addFlight(flight2);

        Booking checkedInBooking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        Payment checkedInPayment = system.processPayment(checkedInBooking.getBookingId(), PaymentMethod.CASH, amount -> true);
        system.checkIn(checkedInBooking.getBookingId());

        Booking noShowConfirmed = system.createBooking("PAX-01", "FL-01", List.of("1B"));
        Payment noShowPayment = system.processPayment(noShowConfirmed.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD, amount -> true);

        Booking pendingUnpaid = system.createBooking("PAX-01", "FL-01", List.of("2A"));

        Booking alreadyCancelled = system.createBooking("PAX-01", "FL-01", List.of("2B"));
        system.cancelBooking(alreadyCancelled.getBookingId());

        Booking otherFlightCheckedIn = system.createBooking("PAX-01", "FL-02", List.of("1A"));
        system.processPayment(otherFlightCheckedIn.getBookingId(), PaymentMethod.CASH, amount -> true);
        system.checkIn(otherFlightCheckedIn.getBookingId());

        // Cannot arrive while SCHEDULED or BOARDING
        assertThrows(InvalidStateException.class, () -> system.arriveFlight("FL-01"));
        flight1.updateStatus(FlightStatus.BOARDING);
        assertThrows(InvalidStateException.class, () -> system.arriveFlight("FL-01"));
        assertEquals(BookingStatus.CHECKED_IN, checkedInBooking.getStatus());
        assertEquals(BookingStatus.PENDING, pendingUnpaid.getStatus());
        assertEquals(SeatStatus.RESERVED, flight1.findSeat("2A").getStatus());

        flight1.updateStatus(FlightStatus.DEPARTED);
        system.arriveFlight("FL-01");

        assertEquals(FlightStatus.ARRIVED, flight1.getStatus());
        assertEquals(BookingStatus.COMPLETED, checkedInBooking.getStatus());
        assertEquals(PaymentStatus.COMPLETED, checkedInPayment.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("1A").getStatus());

        assertEquals(BookingStatus.CANCELLED, pendingUnpaid.getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("2A").getStatus());

        assertEquals(BookingStatus.CONFIRMED, noShowConfirmed.getStatus());
        assertEquals(PaymentStatus.COMPLETED, noShowPayment.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight1.findSeat("1B").getStatus());

        assertEquals(BookingStatus.CANCELLED, alreadyCancelled.getStatus());
        assertEquals(SeatStatus.AVAILABLE, flight1.findSeat("2B").getStatus());

        assertEquals(FlightStatus.SCHEDULED, flight2.getStatus());
        assertEquals(BookingStatus.CHECKED_IN, otherFlightCheckedIn.getStatus());

        // Cannot arrive twice, cannot arrive cancelled flight, validates ID
        assertThrows(InvalidStateException.class, () -> system.arriveFlight("FL-01"));
        system.cancelFlight("FL-02");
        assertThrows(InvalidStateException.class, () -> system.arriveFlight("FL-02"));
        assertThrows(ValidationException.class, () -> system.arriveFlight(null));
        assertThrows(ValidationException.class, () -> system.arriveFlight(" "));
        assertThrows(EntityNotFoundException.class, () -> system.arriveFlight("FL-404"));
    }
}
