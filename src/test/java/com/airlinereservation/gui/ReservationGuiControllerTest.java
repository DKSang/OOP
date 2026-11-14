package com.airlinereservation.gui;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import com.airlinereservation.model.Seat;
import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.model.Payment;
import com.airlinereservation.payment.BankTransferPayment;
import com.airlinereservation.payment.CardPayment;
import com.airlinereservation.payment.CashPayment;
import com.airlinereservation.payment.EWalletPayment;
import com.airlinereservation.payment.PaymentStrategy;
import com.airlinereservation.system.AirlineReservationSystem;
import com.airlinereservation.system.DemoDataSeeder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Test
    void computesLivePortalMetricsAndUpdatesOnBookingCancellationAndReset() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        ReservationGuiController.PortalMetrics initial = controller.getPortalMetrics();
        assertEquals(3, initial.airportsCount());
        assertEquals(3, initial.scheduledFlightsCount());
        assertEquals(49, initial.availableSeatsCount());

        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        controller.bookSeats("FL-01", List.of("1B", "2A"));
        assertEquals(47, controller.getPortalMetrics().availableSeatsCount());

        system.cancelFlight("FL-03");
        ReservationGuiController.PortalMetrics afterCancel = controller.getPortalMetrics();
        assertEquals(2, afterCancel.scheduledFlightsCount());
        assertEquals(31, afterCancel.availableSeatsCount());

        controller.resetDemoData();
        ReservationGuiController.PortalMetrics afterReset = controller.getPortalMetrics();
        assertEquals(3, afterReset.airportsCount());
        assertEquals(3, afterReset.scheduledFlightsCount());
        assertEquals(49, afterReset.availableSeatsCount());
    }

    @Test
    void listsLoggedInPassengerBookingsAndProjectsTableRows() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        assertThrows(ValidationException.class, controller::getPassengerBookings);

        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        assertThrows(ValidationException.class, controller::getPassengerBookings);
        controller.logout();

        controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        List<Booking> pax1Bookings = controller.getPassengerBookings();
        assertEquals(2, pax1Bookings.size());

        List<Object[]> rows = controller.toBookingTableRows(pax1Bookings);
        assertEquals(2, rows.size());

        Object[] confirmedRow = rows.get(0);
        assertEquals("BK-1", confirmedRow[0]);
        assertEquals("VN101", confirmedRow[1]);
        assertEquals("HAN -> SGN", confirmedRow[2]);
        assertEquals("1A", confirmedRow[3]);
        assertEquals("3,000,000 VND", confirmedRow[4]);
        assertEquals(BookingStatus.CONFIRMED, confirmedRow[5]);
        assertEquals(PaymentStatus.COMPLETED, confirmedRow[6]);

        Object[] pendingRow = rows.get(1);
        assertEquals("BK-2", pendingRow[0]);
        assertEquals("VN103", pendingRow[1]);
        assertEquals("HAN -> SGN", pendingRow[2]);
        assertEquals("2A, 2B", pendingRow[3]);
        assertEquals("4,255,000 VND", pendingRow[4]);
        assertEquals(BookingStatus.PENDING, pendingRow[5]);
        assertNull(pendingRow[6]);

        assertEquals(7, ReservationGuiController.BOOKING_TABLE_COLUMNS.length);

        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        assertTrue(controller.getPassengerBookings().isEmpty());
    }

    @Test
    void createsStrategiesViaPaymentFactoryProcessesAllFourMethodsAndSupportsFailureRetry() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        assertInstanceOf(CardPayment.class, controller.createPaymentStrategy(PaymentMethod.CREDIT_DEBIT_CARD, "4111-2222", "An"));
        assertInstanceOf(EWalletPayment.class, controller.createPaymentStrategy(PaymentMethod.E_WALLET, "0901", "MoMo"));
        assertInstanceOf(BankTransferPayment.class, controller.createPaymentStrategy(PaymentMethod.BANK_TRANSFER, "VCB", "12345"));
        assertInstanceOf(CashPayment.class, controller.createPaymentStrategy(PaymentMethod.CASH, "CNT-01", "CSH-01"));

        // Requires logged-in passenger owning the booking
        assertThrows(ValidationException.class, () -> controller.processPayment(
                "BK-2", PaymentMethod.CASH, "CNT-01", "CSH-01", false));

        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        assertThrows(ValidationException.class, () -> controller.processPayment(
                "BK-2", PaymentMethod.CASH, "CNT-01", "CSH-01", false));
        assertThrows(ValidationException.class, () -> controller.validateBookingPayable("BK-2"));

        Booking cardBooking = controller.bookSeats("FL-01", List.of("1B"));
        assertEquals(cardBooking, controller.validateBookingPayable(cardBooking.getBookingId()));
        Booking walletBooking = controller.bookSeats("FL-01", List.of("2B"));
        Booking bankBooking = controller.bookSeats("FL-01", List.of("3B"));
        Booking cashBooking = controller.bookSeats("FL-01", List.of("3C"));

        // Simulated payment failure keeps booking PENDING, seats RESERVED, and surfaces FAILED PaymentStatus
        Payment failed = controller.processPayment(
                cardBooking.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD,
                "4111-2222-3333-4444", "Tran Thi Binh", true
        );
        assertEquals(PaymentStatus.FAILED, failed.getStatus());
        assertEquals(BookingStatus.PENDING, cardBooking.getStatus());
        assertEquals(SeatStatus.RESERVED, system.findFlightById("FL-01").findSeat("1B").getStatus());
        assertEquals(PaymentStatus.FAILED, controller.getBookingPaymentStatus(cardBooking));

        // Blank strategy details still fail validation even with simulateFailure=true
        assertThrows(ValidationException.class, () -> controller.processPayment(
                cardBooking.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD, " ", "Tran Thi Binh", true));

        // Successful retry confirms booking and transitions seats to OCCUPIED
        Payment retryCard = controller.processPayment(
                cardBooking.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD,
                "4111-2222-3333-4444", "Tran Thi Binh", false
        );
        assertEquals(PaymentStatus.COMPLETED, retryCard.getStatus());
        assertEquals(BookingStatus.CONFIRMED, cardBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, system.findFlightById("FL-01").findSeat("1B").getStatus());
        assertEquals(PaymentStatus.COMPLETED, controller.getBookingPaymentStatus(cardBooking));
        assertThrows(InvalidStateException.class, () -> controller.validateBookingPayable(cardBooking.getBookingId()));

        // Remaining three PaymentMethod strategies
        Payment wallet = controller.processPayment(walletBooking.getBookingId(), PaymentMethod.E_WALLET, "0902222222", "MoMo", false);
        Payment bank = controller.processPayment(bankBooking.getBookingId(), PaymentMethod.BANK_TRANSFER, "Vietcombank", "0123456789", false);
        Payment cash = controller.processPayment(cashBooking.getBookingId(), PaymentMethod.CASH, "CNT-01", "CSH-01", false);

        assertEquals(PaymentMethod.E_WALLET, wallet.getMethod());
        assertEquals(PaymentStatus.COMPLETED, wallet.getStatus());
        assertEquals(BookingStatus.CONFIRMED, walletBooking.getStatus());

        assertEquals(PaymentMethod.BANK_TRANSFER, bank.getMethod());
        assertEquals(PaymentStatus.COMPLETED, bank.getStatus());
        assertEquals(BookingStatus.CONFIRMED, bankBooking.getStatus());

        assertEquals(PaymentMethod.CASH, cash.getMethod());
        assertEquals(PaymentStatus.COMPLETED, cash.getStatus());
        assertEquals(BookingStatus.CONFIRMED, cashBooking.getStatus());

        // Reset clears tracked failed attempts too
        controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        controller.processPayment("BK-2", PaymentMethod.E_WALLET, "0901111111", "ZaloPay", true);
        assertEquals(PaymentStatus.FAILED, controller.getBookingPaymentStatus(system.findBookingById("BK-2")));
        controller.resetDemoData();
        assertNull(controller.getBookingPaymentStatus(system.findBookingById("BK-2")));
    }

    @Test
    void checksInAndCancelsOwnedBookingsWithSeatReleaseAndPaymentRefund() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        assertThrows(ValidationException.class, () -> controller.checkInBooking("BK-1"));
        assertThrows(ValidationException.class, () -> controller.cancelBooking("BK-2"));

        // PAX-02 cannot check in or cancel PAX-01's bookings
        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        assertThrows(ValidationException.class, () -> controller.checkInBooking("BK-1"));
        assertThrows(ValidationException.class, () -> controller.cancelBooking("BK-2"));

        // PAX-01 cancels pending BK-2 -> CANCELLED, seats 2A/2B released to AVAILABLE
        controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        assertThrows(InvalidStateException.class, () -> controller.checkInBooking("BK-2"));
        assertTrue(controller.cancelBooking("BK-2"));
        Booking bk2 = system.findBookingById("BK-2");
        assertEquals(BookingStatus.CANCELLED, bk2.getStatus());
        assertEquals(SeatStatus.AVAILABLE, system.findFlightById("FL-02").findSeat("2A").getStatus());
        assertEquals(SeatStatus.AVAILABLE, system.findFlightById("FL-02").findSeat("2B").getStatus());
        assertNull(controller.getBookingPaymentStatus(bk2));
        assertThrows(InvalidStateException.class, () -> controller.cancelBooking("BK-2"));

        // PAX-01 checks in confirmed BK-1 -> CHECKED_IN, seat 1A remains OCCUPIED, cannot cancel afterwards
        Booking checkedIn = controller.checkInBooking("BK-1");
        assertEquals(BookingStatus.CHECKED_IN, checkedIn.getStatus());
        assertEquals(SeatStatus.OCCUPIED, system.findFlightById("FL-01").findSeat("1A").getStatus());
        assertThrows(InvalidStateException.class, () -> controller.checkInBooking("BK-1"));
        assertThrows(InvalidStateException.class, () -> controller.cancelBooking("BK-1"));

        // PAX-02 books, pays, and cancels confirmed booking -> CANCELLED, seat released, payment REFUNDED
        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        Booking paidBooking = controller.bookSeats("FL-01", List.of("1B"));
        controller.processPayment(paidBooking.getBookingId(), PaymentMethod.CASH, "CNT-01", "CSH-01", false);
        assertEquals(BookingStatus.CONFIRMED, paidBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, system.findFlightById("FL-01").findSeat("1B").getStatus());

        assertTrue(controller.cancelBooking(paidBooking.getBookingId()));
        assertEquals(BookingStatus.CANCELLED, paidBooking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, system.findFlightById("FL-01").findSeat("1B").getStatus());
        assertEquals(PaymentStatus.REFUNDED, paidBooking.getPayment().getStatus());
        assertEquals(PaymentStatus.REFUNDED, controller.getBookingPaymentStatus(paidBooking));
    }

    @Test
    void enforcesAdminGuardAndSchedulesFlightsFromCatalogs() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        // Guest and Passenger cannot access admin flight list or schedule flights
        assertThrows(ValidationException.class, controller::getAdminFlights);
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-04", "VN301", "HAN", "DAD",
                "2026-12-20 09:00", "2026-12-20 10:20", "AC-320", "1400000"
        ));

        controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        assertThrows(ValidationException.class, controller::getAdminFlights);
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-04", "VN301", "HAN", "DAD",
                LocalDateTime.of(2026, 12, 20, 9, 0),
                LocalDateTime.of(2026, 12, 20, 10, 20),
                "AC-320", 1_400_000.0
        ));

        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        assertEquals(3, controller.getAdminFlights().size());
        List<Aircraft> aircrafts = controller.getAircrafts();
        assertEquals(2, aircrafts.size());

        Flight scheduled = controller.scheduleFlight(
                "FL-04", "VN301", "HAN", "DAD",
                "2026-12-20 09:00", "2026-12-20 10:20", "AC-320", "1400000"
        );
        assertEquals("FL-04", scheduled.getFlightId());
        assertEquals("VN301", scheduled.getFlightNumber());
        assertEquals("HAN -> DAD", ReservationGuiController.formatRoute(scheduled));
        assertEquals("AC-320", scheduled.getAircraft().getAircraftId());
        assertEquals(16, scheduled.getAvailableSeats().size());
        assertEquals(1_400_000.0, scheduled.getBasePrice(), 1e-6);
        assertEquals(FlightStatus.SCHEDULED, scheduled.getStatus());
        assertEquals(4, controller.getAdminFlights().size());

        // Validation and duplicate handling
        assertThrows(DuplicateEntityException.class, () -> controller.scheduleFlight(
                "FL-04", "VN302", "HAN", "DAD",
                "2026-12-21 09:00", "2026-12-21 10:20", "AC-320", "1400000"
        ));
        assertThrows(EntityNotFoundException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "XXX", "DAD",
                "2026-12-21 09:00", "2026-12-21 10:20", "AC-320", "1400000"
        ));
        assertThrows(EntityNotFoundException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "HAN", "DAD",
                "2026-12-21 09:00", "2026-12-21 10:20", "AC-404", "1400000"
        ));
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "HAN", "HAN",
                "2026-12-21 09:00", "2026-12-21 10:20", "AC-320", "1400000"
        ));
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "HAN", "DAD",
                "20-12-2026 09:00", "2026-12-21 10:20", "AC-320", "1400000"
        ));
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "HAN", "DAD",
                "2026-12-21 10:20", "2026-12-21 09:00", "AC-320", "1400000"
        ));
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "HAN", "DAD",
                "2026-12-21 09:00", "2026-12-21 10:20", "AC-320", "abc"
        ));
        assertThrows(ValidationException.class, () -> controller.scheduleFlight(
                "FL-05", "VN305", "HAN", "DAD",
                "2026-12-21 09:00", "2026-12-21 10:20", "AC-320", "-100"
        ));
    }

    @Test
    void transitionsFlightStatusesAndCascadesArrivalAndCancellationAcrossBookingsAndPayments() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        // Role guards: guest and passenger are blocked from status mutations
        assertThrows(ValidationException.class, () -> controller.updateFlightStatus("FL-01", FlightStatus.BOARDING));
        assertThrows(ValidationException.class, () -> controller.arriveFlight("FL-01"));
        assertThrows(ValidationException.class, () -> controller.cancelFlight("FL-01"));

        controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        assertThrows(ValidationException.class, () -> controller.updateFlightStatus("FL-01", FlightStatus.BOARDING));
        assertThrows(ValidationException.class, () -> controller.arriveFlight("FL-01"));
        assertThrows(ValidationException.class, () -> controller.cancelFlight("FL-01"));

        // Prepare FL-02 with one CHECKED_IN booking (PAX-02) and one PENDING booking (seeded BK-2 of PAX-01)
        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        Booking pax2CheckedIn = controller.bookSeats("FL-02", List.of("1B"));
        controller.processPayment(pax2CheckedIn.getBookingId(), PaymentMethod.CASH, "CNT-01", "CSH-01", false);
        controller.checkInBooking(pax2CheckedIn.getBookingId());
        assertEquals(BookingStatus.CHECKED_IN, pax2CheckedIn.getStatus());

        // Switch to Admin
        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);

        // Invalid direct skips fail with InvalidStateException
        assertThrows(InvalidStateException.class, () -> controller.updateFlightStatus("FL-02", FlightStatus.DEPARTED));
        assertThrows(InvalidStateException.class, () -> controller.arriveFlight("FL-02"));

        // FL-02: SCHEDULED -> BOARDING -> DEPARTED -> ARRIVED cascade
        assertEquals(FlightStatus.BOARDING, controller.updateFlightStatus("FL-02", FlightStatus.BOARDING).getStatus());
        assertEquals(FlightStatus.DEPARTED, controller.updateFlightStatus("FL-02", FlightStatus.DEPARTED).getStatus());
        assertThrows(InvalidStateException.class, () -> controller.cancelFlight("FL-02"));

        Flight arrivedFl02 = controller.arriveFlight("FL-02");
        assertEquals(FlightStatus.ARRIVED, arrivedFl02.getStatus());
        assertEquals(BookingStatus.COMPLETED, pax2CheckedIn.getStatus());
        assertEquals(PaymentStatus.COMPLETED, controller.getBookingPaymentStatus(pax2CheckedIn));

        Booking pendingBk2 = system.findBookingById("BK-2");
        assertEquals(BookingStatus.CANCELLED, pendingBk2.getStatus());
        assertEquals(SeatStatus.AVAILABLE, arrivedFl02.findSeat("2A").getStatus());
        assertEquals(SeatStatus.AVAILABLE, arrivedFl02.findSeat("2B").getStatus());

        List<Object[]> afterArriveRows = controller.toAdminBookingTableRows(controller.getAllBookings());
        assertEquals(BookingStatus.CANCELLED, afterArriveRows.get(1)[6]);
        assertNull(afterArriveRows.get(1)[7]);
        assertEquals(BookingStatus.COMPLETED, afterArriveRows.get(2)[6]);
        assertEquals(PaymentStatus.COMPLETED, afterArriveRows.get(2)[7]);

        // FL-01: SCHEDULED -> BOARDING -> CANCELLED cascade refunds BK-1 and releases seat 1A
        assertEquals(FlightStatus.BOARDING, controller.updateFlightStatus("FL-01", FlightStatus.BOARDING).getStatus());
        Flight cancelledFl01 = controller.cancelFlight("FL-01");
        assertEquals(FlightStatus.CANCELLED, cancelledFl01.getStatus());

        Booking bk1 = system.findBookingById("BK-1");
        assertEquals(BookingStatus.CANCELLED, bk1.getStatus());
        assertEquals(PaymentStatus.REFUNDED, bk1.getPayment().getStatus());
        assertEquals(PaymentStatus.REFUNDED, controller.getBookingPaymentStatus(bk1));
        assertEquals(SeatStatus.AVAILABLE, cancelledFl01.findSeat("1A").getStatus());

        // updateFlightStatus with ARRIVED / CANCELLED also routes through system cascades
        Flight fl04 = controller.scheduleFlight(
                "FL-04", "VN304", "HAN", "DAD",
                "2026-12-20 09:00", "2026-12-20 10:20", "AC-320", "1400000"
        );
        controller.login("PAX-02", Person.DEFAULT_PASSWORD);
        Booking fl04Booking = controller.bookSeats(fl04.getFlightId(), List.of("1A"));
        controller.processPayment(fl04Booking.getBookingId(), PaymentMethod.E_WALLET, "0902", "MoMo", false);

        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        controller.updateFlightStatus("FL-04", FlightStatus.CANCELLED);
        assertEquals(FlightStatus.CANCELLED, fl04.getStatus());
        assertEquals(BookingStatus.CANCELLED, fl04Booking.getStatus());
        assertEquals(PaymentStatus.REFUNDED, controller.getBookingPaymentStatus(fl04Booking));
    }

    @Test
    void projectsSystemWideBookingsPassengersAndAdminMetricsWithRoleGuards() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();
        ReservationGuiController controller = new ReservationGuiController(system);

        // Role guards: guest and passenger cannot query system-wide bookings, passengers, or admin metrics
        assertThrows(ValidationException.class, controller::getAllBookings);
        assertThrows(ValidationException.class, controller::getAllPassengers);
        assertThrows(ValidationException.class, controller::getAdminMetrics);

        controller.login("PAX-01", Person.DEFAULT_PASSWORD);
        assertThrows(ValidationException.class, controller::getAllBookings);
        assertThrows(ValidationException.class, controller::getAllPassengers);
        assertThrows(ValidationException.class, controller::getAdminMetrics);

        // Admin views initial seeded bookings, passengers, and metrics
        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        List<Booking> bookings = controller.getAllBookings();
        List<Passenger> passengers = controller.getAllPassengers();
        assertEquals(2, bookings.size());
        assertEquals(2, passengers.size());

        assertEquals(8, ReservationGuiController.ADMIN_BOOKING_TABLE_COLUMNS.length);
        assertEquals(8, ReservationGuiController.PASSENGER_TABLE_COLUMNS.length);

        List<Object[]> bookingRows = controller.toAdminBookingTableRows(bookings);
        assertEquals(2, bookingRows.size());
        Object[] bk1Row = bookingRows.get(0);
        assertEquals("BK-1", bk1Row[0]);
        assertEquals("PAX-01 · Nguyen Van An", bk1Row[1]);
        assertEquals("VN101", bk1Row[2]);
        assertEquals("HAN -> SGN", bk1Row[3]);
        assertEquals("1A", bk1Row[4]);
        assertEquals("3,000,000 VND", bk1Row[5]);
        assertEquals(BookingStatus.CONFIRMED, bk1Row[6]);
        assertEquals(PaymentStatus.COMPLETED, bk1Row[7]);

        Object[] bk2Row = bookingRows.get(1);
        assertEquals("BK-2", bk2Row[0]);
        assertEquals("PAX-01 · Nguyen Van An", bk2Row[1]);
        assertEquals(BookingStatus.PENDING, bk2Row[6]);
        assertNull(bk2Row[7]);

        List<Object[]> passengerRows = controller.toPassengerTableRows(passengers);
        assertEquals(2, passengerRows.size());
        Object[] pax1Row = passengerRows.get(0);
        assertEquals("PAX-01", pax1Row[0]);
        assertEquals("Nguyen Van An", pax1Row[1]);
        assertEquals("an.nguyen@airline.vn", pax1Row[2]);
        assertEquals("0901111111", pax1Row[3]);
        assertEquals("1998-01-10", pax1Row[4]);
        assertEquals("Male", pax1Row[5]);
        assertEquals("CCCD-0001", pax1Row[6]);
        assertEquals(2, pax1Row[7]);

        Object[] pax2Row = passengerRows.get(1);
        assertEquals("PAX-02", pax2Row[0]);
        assertEquals(0, pax2Row[7]);

        ReservationGuiController.AdminMetrics initialMetrics = controller.getAdminMetrics();
        assertEquals(3, initialMetrics.totalFlightsCount());
        assertEquals(2, initialMetrics.totalBookingsCount());
        assertEquals(2, initialMetrics.registeredPassengersCount());
        assertEquals(3_000_000.0, initialMetrics.settledRevenue(), 1e-6);

        // Cancelling FL-01 cascades to BK-1 -> CANCELLED / REFUNDED and updates revenue metric
        controller.cancelFlight("FL-01");
        List<Object[]> afterCancelRows = controller.toAdminBookingTableRows(controller.getAllBookings());
        assertEquals(BookingStatus.CANCELLED, afterCancelRows.get(0)[6]);
        assertEquals(PaymentStatus.REFUNDED, afterCancelRows.get(0)[7]);
        assertEquals(0.0, controller.getAdminMetrics().settledRevenue(), 1e-6);

        // Reset restores seeded state and clears Admin session
        controller.resetDemoData();
        assertFalse(controller.isAdminLoggedIn());
        controller.login("ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD);
        ReservationGuiController.AdminMetrics resetMetrics = controller.getAdminMetrics();
        assertEquals(3, resetMetrics.totalFlightsCount());
        assertEquals(2, resetMetrics.totalBookingsCount());
        assertEquals(2, resetMetrics.registeredPassengersCount());
        assertEquals(3_000_000.0, resetMetrics.settledRevenue(), 1e-6);
    }
}
