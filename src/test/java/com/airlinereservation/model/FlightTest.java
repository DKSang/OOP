package com.airlinereservation.model;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightTest {

    private static final Airport HAN = new Airport("HAN", "Noi Bai", "Hanoi");
    private static final Airport SGN = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
    private static final LocalDateTime DEP_TIME = LocalDateTime.of(2026, 12, 1, 8, 30);
    private static final LocalDateTime ARR_TIME = LocalDateTime.of(2026, 12, 1, 10, 45);

    @Test
    void generatesDefaultFourColumnSeatMapForSmallAndStandardAircrafts() {
        Aircraft small = new Aircraft("AC-SMALL", "Cessna", "Cessna", 4);
        Flight smallFlight = new Flight(" FL-1 ", " VN101 ", HAN, SGN, DEP_TIME, ARR_TIME, small, 1_200_000.0);

        assertEquals("FL-1", smallFlight.getFlightId());
        assertEquals("VN101", smallFlight.getFlightNumber());
        assertEquals(FlightStatus.SCHEDULED, smallFlight.getStatus());
        assertEquals(1_200_000.0, smallFlight.getBasePrice(), 0.001);

        List<Seat> smallSeats = smallFlight.getSeats();
        assertEquals(List.of("1A", "1B", "1C", "1D"),
                smallSeats.stream().map(Seat::getSeatNumber).toList());
        for (Seat seat : smallSeats) {
            EconomySeat econ = assertInstanceOf(EconomySeat.class, seat);
            assertTrue(econ.hasExtraLegroom());
        }

        Aircraft medium = new Aircraft("AC-MED", "ATR72", "ATR", 10);
        Flight mediumFlight = new Flight("FL-2", "VN102", HAN, SGN, DEP_TIME, ARR_TIME, medium, 1_500_000.0);
        List<Seat> mediumSeats = mediumFlight.getSeats();
        assertEquals(List.of("1A", "1B", "1C", "1D", "2A", "2B", "2C", "2D", "3A", "3B"),
                mediumSeats.stream().map(Seat::getSeatNumber).toList());
        assertInstanceOf(BusinessSeat.class, mediumSeats.get(0));
        assertInstanceOf(BusinessSeat.class, mediumSeats.get(1));
        assertTrue(assertInstanceOf(EconomySeat.class, mediumSeats.get(2)).hasExtraLegroom());
        assertTrue(assertInstanceOf(EconomySeat.class, mediumSeats.get(3)).hasExtraLegroom());
        assertFalse(assertInstanceOf(EconomySeat.class, mediumSeats.get(4)).hasExtraLegroom());
        assertFalse(assertInstanceOf(EconomySeat.class, mediumSeats.get(9)).hasExtraLegroom());
    }

    @Test
    void supportsExplicitBusinessSeatCountIncludingZeroAndFullCapacity() {
        Aircraft aircraft = new Aircraft("AC-6", "Regional", "Embraer", 6);

        Flight allEconomy = new Flight("FL-E", "VN201", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000.0, 0);
        assertEquals(0, allEconomy.getSeats().stream().filter(BusinessSeat.class::isInstance).count());
        assertTrue(assertInstanceOf(EconomySeat.class, allEconomy.getSeats().get(0)).hasExtraLegroom());
        assertTrue(assertInstanceOf(EconomySeat.class, allEconomy.getSeats().get(3)).hasExtraLegroom());
        assertFalse(assertInstanceOf(EconomySeat.class, allEconomy.getSeats().get(4)).hasExtraLegroom());

        Flight allBusiness = new Flight("FL-B", "VN202", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000.0, 6);
        assertEquals(6, allBusiness.getSeats().stream().filter(BusinessSeat.class::isInstance).count());
    }

    @Test
    void rejectsInvalidFlightInvariantsAndBusinessSeatCount() {
        Aircraft aircraft = new Aircraft("AC-10", "A321", "Airbus", 10);
        Airport hanDuplicateCode = new Airport(" han ", "Noi Bai 2", "Hanoi");

        assertThrows(ValidationException.class,
                () -> new Flight(" ", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", null, HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", null, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, null, DEP_TIME, ARR_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, hanDuplicateCode, DEP_TIME, ARR_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, null, ARR_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, null, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, DEP_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, ARR_TIME, DEP_TIME, aircraft, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, null, 1_000_000));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 0));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, -100));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000, -1));
        assertThrows(ValidationException.class,
                () -> new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000, 11));
    }

    @Test
    void keepsSeatStateIsolatedAcrossFlightsSharingOneAircraftAndReturnsImmutableLists() {
        Aircraft shared = new Aircraft("AC-SHARED", "A320", "Airbus", 6);
        Flight morning = new Flight("FL-AM", "VN301", HAN, SGN, DEP_TIME, ARR_TIME, shared, 1_000_000);
        Flight evening = new Flight("FL-PM", "VN302", HAN, SGN,
                DEP_TIME.plusHours(8), ARR_TIME.plusHours(8), shared, 1_000_000);

        Seat morning1A = morning.findSeat("1a");
        Seat evening1A = evening.findSeat("1A");
        assertNotSame(morning1A, evening1A);

        morning1A.reserve();
        assertEquals(SeatStatus.RESERVED, morning.findSeat("1A").getStatus());
        assertEquals(SeatStatus.AVAILABLE, evening.findSeat("1A").getStatus());
        assertEquals(5, morning.getAvailableSeats().size());
        assertEquals(6, evening.getAvailableSeats().size());
        assertTrue(morning.hasAvailableSeats(5));
        assertFalse(morning.hasAvailableSeats(6));

        assertThrows(UnsupportedOperationException.class, () -> morning.getSeats().add(evening1A));
        assertThrows(UnsupportedOperationException.class, () -> morning.getAvailableSeats().removeFirst());
        assertThrows(EntityNotFoundException.class, () -> morning.findSeat("9Z"));
        assertThrows(ValidationException.class, () -> morning.findSeat(" "));
    }

    @Test
    void matchesScheduledFlightsCaseInsensitivelyAndEnforcesLifecycleTransitions() {
        Aircraft aircraft = new Aircraft("AC-1", "A321", "Airbus", 4);
        Flight flight = new Flight("FL-1", "VN101", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000);

        assertTrue(flight.matches("han", " sgn ", LocalDate.of(2026, 12, 1)));
        assertFalse(flight.matches("SGN", "HAN", LocalDate.of(2026, 12, 1)));
        assertFalse(flight.matches("HAN", "SGN", LocalDate.of(2026, 12, 2)));

        assertThrows(InvalidStateException.class, () -> flight.updateStatus(FlightStatus.DEPARTED));
        assertThrows(InvalidStateException.class, () -> flight.updateStatus(FlightStatus.SCHEDULED));

        flight.updateStatus(FlightStatus.BOARDING);
        assertEquals(FlightStatus.BOARDING, flight.getStatus());
        assertFalse(flight.matches("HAN", "SGN", LocalDate.of(2026, 12, 1)));

        flight.updateStatus(FlightStatus.DEPARTED);
        assertThrows(InvalidStateException.class, flight::cancel);
        assertThrows(InvalidStateException.class, () -> flight.updateStatus(FlightStatus.BOARDING));

        flight.updateStatus(FlightStatus.ARRIVED);
        assertEquals(FlightStatus.ARRIVED, flight.getStatus());

        Flight cancellable = new Flight("FL-2", "VN102", HAN, SGN, DEP_TIME, ARR_TIME, aircraft, 1_000_000);
        cancellable.cancel();
        assertEquals(FlightStatus.CANCELLED, cancellable.getStatus());
        assertThrows(InvalidStateException.class, () -> cancellable.updateStatus(FlightStatus.BOARDING));
    }
}
