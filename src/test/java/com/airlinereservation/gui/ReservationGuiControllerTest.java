package com.airlinereservation.gui;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import com.airlinereservation.model.Seat;
import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.system.AirlineReservationSystem;
import com.airlinereservation.system.DemoDataSeeder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationGuiControllerTest {

    @Test
    void managesPassengerAndAdminLoginLogoutSessions() {
        ReservationGuiController controller = new ReservationGuiController(DemoDataSeeder.createSeededSystem());

        assertNull(controller.getCurrentUser());
        assertFalse(controller.isPassengerLoggedIn());
        assertFalse(controller.isAdminLoggedIn());

        Person passenger = controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        assertInstanceOf(Passenger.class, passenger);
        assertEquals("PAX-01", controller.getCurrentUser().getId());
        assertTrue(controller.isPassengerLoggedIn());
        assertFalse(controller.isAdminLoggedIn());

        controller.logout();
        assertNull(controller.getCurrentUser());
        assertFalse(controller.isPassengerLoggedIn());

        Person admin = controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        assertInstanceOf(Admin.class, admin);
        assertTrue(controller.isAdminLoggedIn());
        assertFalse(controller.isPassengerLoggedIn());

        assertThrows(ValidationException.class, () -> controller.login("ADM-01", "wrong-pass"));
        assertTrue(controller.isAdminLoggedIn());
    }

    @Test
    void providesQuickFillDemoAccountsRegistersNewPassengerAndResetsDemoData() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        List<DemoAccount> accounts = controller.getDemoAccounts();
        assertEquals(3, accounts.size());
        for (DemoAccount account : accounts) {
            Person loggedIn = controller.login(account.identifier(), account.password());
            assertEquals(account.role(), loggedIn.getRole());
            controller.logout();
        }

        Passenger registered = controller.registerPassenger(
                "PAX-03",
                "Pham Gia Huy",
                "huy.pham@airline.vn",
                "0903334444",
                LocalDate.of(2002, 7, 15),
                "Male",
                "CCCD-0003",
                "mySecret99"
        );
        assertEquals("PAX-03", registered.getId());
        Person loggedInNew = controller.login("huy.pham@airline.vn", "mySecret99");
        assertEquals("PAX-03", loggedInNew.getId());
        assertTrue(controller.isPassengerLoggedIn());

        assertThrows(DuplicateEntityException.class, () -> controller.registerPassenger(
                "PAX-03", "Dup", "dup@airline.vn", "0900",
                LocalDate.of(2000, 1, 1), "Male", "CCCD-9999", "pass123"
        ));
        assertThrows(ValidationException.class, () -> controller.registerPassenger(
                "PAX-04", "Bad Date", "bad@airline.vn", "0900",
                "15-07-2002", "Female", "CCCD-8888", "pass123"
        ));

        controller.resetDemoData();
        assertNull(controller.getCurrentUser());
        assertFalse(controller.isPassengerLoggedIn());
        assertEquals(2, system.getPassengers().size());
        assertThrows(ValidationException.class, () -> controller.login("PAX-03", "mySecret99"));
    }

    @Test
    void listsAirportsSearchesFlightsAndFormatsTableRows() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        assertEquals(3, controller.getAirports().size());
        assertEquals(3, controller.getAllFlights().size());
        assertEquals(3, controller.getScheduledFlights().size());
        assertEquals(DemoDataSeeder.DEFAULT_TRAVEL_DATE, controller.getDefaultTravelDate());

        List<Flight> hanToSgn = controller.searchFlights(
                "han",
                "SGN",
                DemoDataSeeder.DEFAULT_TRAVEL_DATE,
                2
        );
        assertEquals(2, hanToSgn.size());
        assertEquals("FL-01", hanToSgn.get(0).getFlightId());
        assertEquals("FL-02", hanToSgn.get(1).getFlightId());

        List<Object[]> rows = controller.toFlightTableRows(hanToSgn);
        assertEquals(2, rows.size());
        Object[] fl01Row = rows.get(0);
        assertEquals("FL-01", fl01Row[0]);
        assertEquals("VN101", fl01Row[1]);
        assertEquals("HAN -> SGN", fl01Row[2]);
        assertEquals("2026-12-15 08:00", fl01Row[3]);
        assertEquals("2026-12-15 10:15", fl01Row[4]);
        assertEquals("A320neo", fl01Row[5]);
        assertEquals(15, fl01Row[6]);
        assertEquals("1,500,000 VND", fl01Row[7]);
        assertEquals(FlightStatus.SCHEDULED, fl01Row[8]);

        List<Flight> highCapacityOnly = controller.searchFlights(
                "HAN",
                "SGN",
                DemoDataSeeder.DEFAULT_TRAVEL_DATE,
                18
        );
        assertEquals(1, highCapacityOnly.size());
        assertEquals("FL-02", highCapacityOnly.get(0).getFlightId());

        List<Flight> parsedSearch = controller.searchFlights("HAN", "SGN", "2026-12-15", "2");
        assertEquals(2, parsedSearch.size());

        system.cancelFlight("FL-03");
        assertEquals(3, controller.getAllFlights().size());
        assertEquals(2, controller.getScheduledFlights().size());

        assertThrows(ValidationException.class, () -> controller.searchFlights("HAN", "SGN", "15-12-2026", "1"));
        assertThrows(ValidationException.class, () -> controller.searchFlights("HAN", "SGN", "2026-12-15", "0"));
        assertThrows(ValidationException.class, () -> controller.searchFlights(" ", "SGN", "2026-12-15", "1"));
        assertThrows(ValidationException.class, () -> controller.searchFlights("HAN", "SGN", "2026-12-15", "abc"));
        assertThrows(ValidationException.class, () -> controller.searchFlights("HAN", "SGN", "2026-12-15", " "));
    }

    @Test
    void inspectsFlightSeatsAndPreviewsLiveSelectionPriceAcrossCabinTypes() {
        ReservationGuiController controller = new ReservationGuiController(DemoDataSeeder.createSeededSystem());

        List<Seat> fl01Seats = controller.getSeatsForFlight("FL-01");
        assertEquals(16, fl01Seats.size());
        assertEquals(SeatStatus.OCCUPIED, fl01Seats.get(0).getStatus());
        assertEquals(SeatStatus.AVAILABLE, fl01Seats.get(1).getStatus());

        assertEquals(0.0, controller.previewSeatSelectionPrice("FL-01", List.of()), 1e-6);
        assertEquals(3_000_000.0, controller.previewSeatSelectionPrice("FL-01", List.of("1B")), 1e-6);
        assertEquals(1_725_000.0, controller.previewSeatSelectionPrice("FL-01", List.of("2A")), 1e-6);
        assertEquals(1_500_000.0, controller.previewSeatSelectionPrice("FL-01", List.of("3A")), 1e-6);
        assertEquals(
                6_225_000.0,
                controller.previewSeatSelectionPrice("FL-01", List.of("1B", "2A", "3A")),
                1e-6
        );

        assertThrows(EntityNotFoundException.class, () -> controller.getSeatsForFlight("FL-404"));
        assertThrows(EntityNotFoundException.class, () -> controller.previewSeatSelectionPrice("FL-01", List.of("9Z")));
        assertThrows(ValidationException.class, () -> controller.previewSeatSelectionPrice("FL-01", List.of("1B", "1B")));
        assertThrows(ValidationException.class, () -> controller.previewSeatSelectionPrice("FL-01", null));
    }

    @Test
    void booksSeatsForLoggedInPassengerUpdatesAvailabilityAndPropagatesDomainExceptions() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        assertThrows(ValidationException.class, () -> controller.bookSeats("FL-01", List.of("1B")));

        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        assertThrows(ValidationException.class, () -> controller.bookSeats("FL-01", List.of("1B")));
        controller.logout();

        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        Booking booking = controller.bookSeats("FL-01", List.of("1B", "2A"));
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals("PAX-02", booking.getPassenger().getId());
        assertEquals("FL-01", booking.getFlight().getFlightId());
        assertEquals(4_725_000.0, booking.getTotalPrice(), 1e-6);
        assertEquals(SeatStatus.RESERVED, system.findFlightById("FL-01").findSeat("1B").getStatus());
        assertEquals(SeatStatus.RESERVED, system.findFlightById("FL-01").findSeat("2A").getStatus());

        Object[] updatedFl01Row = controller.toFlightTableRows(List.of(controller.findFlight("FL-01"))).get(0);
        assertEquals(13, updatedFl01Row[6]);

        assertThrows(SeatNotAvailableException.class, () -> controller.bookSeats("FL-01", List.of("1A")));
        assertThrows(SeatNotAvailableException.class, () -> controller.bookSeats("FL-01", List.of("1B")));
        assertThrows(ValidationException.class, () -> controller.bookSeats("FL-01", List.of()));

        system.cancelFlight("FL-03");
        assertThrows(InvalidStateException.class, () -> controller.bookSeats("FL-03", List.of("1A")));
    }
}
