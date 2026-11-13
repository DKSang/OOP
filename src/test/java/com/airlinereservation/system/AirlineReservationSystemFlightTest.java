package com.airlinereservation.system;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Flight;
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
}
