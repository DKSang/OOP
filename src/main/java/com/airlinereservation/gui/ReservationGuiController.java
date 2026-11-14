package com.airlinereservation.gui;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Payment;
import com.airlinereservation.model.Person;
import com.airlinereservation.model.Seat;
import com.airlinereservation.payment.PaymentFactory;
import com.airlinereservation.payment.PaymentStrategy;
import com.airlinereservation.system.AirlineReservationSystem;
import com.airlinereservation.system.DemoDataSeeder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Passive-View controller mediating between JavaFX views and {@link AirlineReservationSystem}.
 */
public class ReservationGuiController {

    public static final String[] FLIGHT_TABLE_COLUMNS = {
            "Flight ID", "Flight No", "Route", "Departure", "Arrival",
            "Aircraft", "Available Seats", "Base Price", "Status"
    };

    public static final String[] BOOKING_TABLE_COLUMNS = {
            "Booking ID", "Flight No", "Route", "Seats",
            "Total Price", "Booking Status", "Payment Status"
    };

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("Passenger - PAX-01 (Nguyen Van An)", "PAX-01", Person.DEFAULT_PASSWORD, "Passenger"),
            new DemoAccount("Passenger - PAX-02 (Tran Thi Binh)", "PAX-02", Person.DEFAULT_PASSWORD, "Passenger"),
            new DemoAccount("Admin - ADM-01 (Le Minh Cuong)", "ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD, "Admin")
    );

    private final AirlineReservationSystem system;
    private final Map<String, Payment> latestPaymentAttempts = new HashMap<>();
    private Person currentUser;

    public ReservationGuiController(AirlineReservationSystem system) {
        this.system = ValidationException.requireNonNull(system, "Reservation system");
    }

    public Person login(String identifier, String password) {
        Person authenticated = system.authenticate(identifier, password);
        this.currentUser = authenticated;
        return authenticated;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Person getCurrentUser() {
        return currentUser;
    }

    public boolean isPassengerLoggedIn() {
        return currentUser instanceof Passenger;
    }

    public boolean isAdminLoggedIn() {
        return currentUser instanceof Admin;
    }

    public Passenger registerPassenger(String id,
                                       String fullName,
                                       String email,
                                       String phoneNumber,
                                       String dateOfBirthText,
                                       String gender,
                                       String passportOrId,
                                       String password) {
        LocalDate dateOfBirth = parseIsoDate(dateOfBirthText, "Date of birth");
        return registerPassenger(id, fullName, email, phoneNumber, dateOfBirth, gender, passportOrId, password);
    }

    public Passenger registerPassenger(String id,
                                       String fullName,
                                       String email,
                                       String phoneNumber,
                                       LocalDate dateOfBirth,
                                       String gender,
                                       String passportOrId,
                                       String password) {
        Passenger passenger = new Passenger(
                id, fullName, email, phoneNumber, dateOfBirth, gender, passportOrId, password
        );
        system.registerPassenger(passenger);
        return passenger;
    }

    public List<DemoAccount> getDemoAccounts() {
        return DEMO_ACCOUNTS;
    }

    public void resetDemoData() {
        DemoDataSeeder.reset(system);
        this.currentUser = null;
        this.latestPaymentAttempts.clear();
    }

    public LocalDate getDefaultTravelDate() {
        return DemoDataSeeder.DEFAULT_TRAVEL_DATE;
    }

    public List<Airport> getAirports() {
        return system.getAirports();
    }

    public List<Flight> getAllFlights() {
        return system.getFlights();
    }

    public List<Flight> getScheduledFlights() {
        return system.getFlights().stream()
                .filter(Flight::canAcceptNewBooking)
                .toList();
    }

    public List<Flight> searchFlights(String depCode, String arrCode, String dateText, String seatCountText) {
        LocalDate date = parseIsoDate(dateText, "Travel date");
        String trimmedCount = ValidationException.requireNonBlank(seatCountText, "Seat count");
        int seatCount;
        try {
            seatCount = Integer.parseInt(trimmedCount);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Seat count must be a positive integer", ex);
        }
        return searchFlights(depCode, arrCode, date, seatCount);
    }

    public List<Flight> searchFlights(String depCode, String arrCode, LocalDate date, int seatCount) {
        return system.searchFlights(depCode, arrCode, date, seatCount);
    }

    public Flight findFlight(String flightId) {
        return system.findFlightById(flightId);
    }

    public List<Seat> getSeatsForFlight(String flightId) {
        return findFlight(flightId).getSeats();
    }

    public double previewSeatSelectionPrice(String flightId, List<String> seatNumbers) {
        Flight flight = findFlight(flightId);
        ValidationException.requireNonNull(seatNumbers, "Seat numbers");
        if (seatNumbers.isEmpty()) {
            return 0.0;
        }
        Set<String> seen = new HashSet<>();
        double total = 0.0;
        double basePrice = flight.getBasePrice();
        for (String seatNumber : seatNumbers) {
            String normalized = ValidationException.requireNonBlank(seatNumber, "Seat number")
                    .toUpperCase(Locale.ROOT);
            if (!seen.add(normalized)) {
                throw new ValidationException("Seat numbers must not contain duplicates: " + normalized);
            }
            Seat seat = flight.findSeat(normalized);
            total += seat.calculatePrice(basePrice);
        }
        return total;
    }

    public Booking bookSeats(String flightId, List<String> seatNumbers) {
        Passenger passenger = requireLoggedInPassenger();
        return system.createBooking(passenger.getId(), flightId, seatNumbers);
    }

    public List<Booking> getPassengerBookings() {
        return requireLoggedInPassenger().getBookingHistory();
    }

    public PaymentStatus getBookingPaymentStatus(Booking booking) {
        ValidationException.requireNonNull(booking, "Booking");
        Payment payment = booking.getPayment();
        if (payment != null) {
            return payment.getStatus();
        }
        Payment latestAttempt = latestPaymentAttempts.get(booking.getBookingId());
        return latestAttempt != null ? latestAttempt.getStatus() : null;
    }

    public PaymentStrategy createPaymentStrategy(PaymentMethod method, String primaryDetail, String secondaryDetail) {
        ValidationException.requireNonNull(method, "Payment method");
        return switch (method) {
            case CREDIT_DEBIT_CARD -> PaymentFactory.createCardPayment(primaryDetail, secondaryDetail);
            case E_WALLET -> PaymentFactory.createEWalletPayment(primaryDetail, secondaryDetail);
            case BANK_TRANSFER -> PaymentFactory.createBankTransferPayment(primaryDetail, secondaryDetail);
            case CASH -> PaymentFactory.createCashPayment(primaryDetail, secondaryDetail);
        };
    }

    public Payment processPayment(String bookingId,
                                  PaymentMethod method,
                                  String primaryDetail,
                                  String secondaryDetail,
                                  boolean simulateFailure) {
        Booking booking = requireOwnedBooking(bookingId);
        PaymentStrategy strategy = createPaymentStrategy(method, primaryDetail, secondaryDetail);
        PaymentStrategy executionStrategy = simulateFailure ? failingStrategy(method) : strategy;
        Payment payment = system.processPayment(booking.getBookingId(), method, executionStrategy);
        latestPaymentAttempts.put(booking.getBookingId(), payment);
        return payment;
    }

    public Booking validateBookingPayable(String bookingId) {
        Booking booking = requireOwnedBooking(bookingId);
        booking.validateConfirmable();
        return booking;
    }

    public Booking checkInBooking(String bookingId) {
        Booking booking = requireOwnedBooking(bookingId);
        system.checkIn(booking.getBookingId());
        return booking;
    }

    public boolean cancelBooking(String bookingId) {
        Booking booking = requireOwnedBooking(bookingId);
        return system.cancelBooking(booking.getBookingId());
    }

    private Booking requireOwnedBooking(String bookingId) {
        Passenger passenger = requireLoggedInPassenger();
        Booking booking = system.findBookingById(bookingId);
        if (booking.getPassenger() != passenger) {
            throw new ValidationException("Booking " + booking.getBookingId()
                    + " does not belong to logged-in passenger " + passenger.getId());
        }
        return booking;
    }

    private static PaymentStrategy failingStrategy(PaymentMethod method) {
        return new PaymentStrategy() {
            @Override
            public boolean pay(double amount) {
                return false;
            }

            @Override
            public PaymentMethod getPaymentMethod() {
                return method;
            }
        };
    }

    private Passenger requireLoggedInPassenger() {
        if (!(currentUser instanceof Passenger passenger)) {
            throw new ValidationException("Passenger login is required for this action");
        }
        return passenger;
    }

    private static LocalDate parseIsoDate(String dateText, String fieldName) {
        String trimmed = ValidationException.requireNonBlank(dateText, fieldName);
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(fieldName + " must follow YYYY-MM-DD format", ex);
        }
    }

    public List<Object[]> toFlightTableRows(List<Flight> flights) {
        ValidationException.requireNonNull(flights, "Flights");
        return flights.stream()
                .map(flight -> new Object[]{
                        flight.getFlightId(),
                        flight.getFlightNumber(),
                        formatRoute(flight),
                        DATE_TIME_FORMATTER.format(flight.getDepartureTime()),
                        DATE_TIME_FORMATTER.format(flight.getArrivalTime()),
                        flight.getAircraft().getModel(),
                        flight.getAvailableSeats().size(),
                        formatCurrency(flight.getBasePrice()),
                        flight.getStatus()
                })
                .toList();
    }

    public List<Object[]> toBookingTableRows(List<Booking> bookings) {
        ValidationException.requireNonNull(bookings, "Bookings");
        return bookings.stream()
                .map(booking -> new Object[]{
                        booking.getBookingId(),
                        booking.getFlight().getFlightNumber(),
                        formatRoute(booking.getFlight()),
                        formatSeatNumbers(booking),
                        formatCurrency(booking.getTotalPrice()),
                        booking.getStatus(),
                        getBookingPaymentStatus(booking)
                })
                .toList();
    }

    public static String formatRoute(Flight flight) {
        ValidationException.requireNonNull(flight, "Flight");
        return flight.getDepartureAirport().getAirportCode()
                + " -> " + flight.getArrivalAirport().getAirportCode();
    }

    public static String formatSeatNumbers(Booking booking) {
        ValidationException.requireNonNull(booking, "Booking");
        return booking.getSelectedSeats().stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.joining(", "));
    }

    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f VND", amount);
    }

    public PortalMetrics getPortalMetrics() {
        List<Flight> scheduled = getScheduledFlights();
        int availableSeats = scheduled.stream()
                .mapToInt(flight -> flight.getAvailableSeats().size())
                .sum();
        return new PortalMetrics(system.getAirports().size(), scheduled.size(), availableSeats);
    }

    /**
     * Live network metrics displayed in the Web3 login showcase panel.
     */
    public record PortalMetrics(int airportsCount, int scheduledFlightsCount, int availableSeatsCount) {
    }
}
