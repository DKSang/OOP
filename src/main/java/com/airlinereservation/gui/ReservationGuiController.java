package com.airlinereservation.gui;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Aircraft;
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
import java.time.LocalDateTime;
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

    public static final String[] ADMIN_BOOKING_TABLE_COLUMNS = {
            "Booking ID", "Passenger", "Flight No", "Route", "Seats",
            "Total Price", "Booking Status", "Payment Status"
    };

    public static final String[] PASSENGER_TABLE_COLUMNS = {
            "Passenger ID", "Full Name", "Email", "Phone",
            "Date of Birth", "Gender", "Passport / ID", "Bookings"
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

    public String generateNextPassengerId() {
        Set<String> existingIds = system.getPassengers().stream()
                .map(p -> p.getId().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        int seq = system.getPassengers().size() + 1;
        while (true) {
            String candidate = String.format(Locale.ROOT, "PAX-%02d", seq);
            if (!existingIds.contains(candidate)) {
                return candidate;
            }
            seq++;
        }
    }

    public Passenger registerPassenger(String fullName,
                                       String email,
                                       String phoneNumber,
                                       String dateOfBirthText,
                                       String gender,
                                       String passportOrId,
                                       String password) {
        return registerPassenger(
                generateNextPassengerId(),
                fullName,
                email,
                phoneNumber,
                dateOfBirthText,
                gender,
                passportOrId,
                password
        );
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

    public List<Aircraft> getAircrafts() {
        return system.getAircrafts();
    }

    public List<Flight> getAllFlights() {
        return system.getFlights();
    }

    public List<Flight> getAdminFlights() {
        requireLoggedInAdmin();
        return system.getFlights();
    }

    public Flight scheduleFlight(String flightId,
                                 String flightNumber,
                                 String departureAirportCode,
                                 String arrivalAirportCode,
                                 String departureTimeText,
                                 String arrivalTimeText,
                                 String aircraftId,
                                 String basePriceText) {
        requireLoggedInAdmin();
        LocalDateTime departureTime = parseDateTime(departureTimeText, "Departure time");
        LocalDateTime arrivalTime = parseDateTime(arrivalTimeText, "Arrival time");
        double basePrice = parsePositiveDouble(basePriceText, "Base price");
        return scheduleFlight(
                flightId,
                flightNumber,
                departureAirportCode,
                arrivalAirportCode,
                departureTime,
                arrivalTime,
                aircraftId,
                basePrice
        );
    }

    public Flight scheduleFlight(String flightId,
                                 String flightNumber,
                                 String departureAirportCode,
                                 String arrivalAirportCode,
                                 LocalDateTime departureTime,
                                 LocalDateTime arrivalTime,
                                 String aircraftId,
                                 double basePrice) {
        requireLoggedInAdmin();
        Airport departure = system.findAirportByCode(departureAirportCode);
        Airport arrival = system.findAirportByCode(arrivalAirportCode);
        Aircraft aircraft = system.findAircraftById(aircraftId);
        Flight flight = new Flight(
                flightId,
                flightNumber,
                departure,
                arrival,
                departureTime,
                arrivalTime,
                aircraft,
                basePrice
        );
        system.addFlight(flight);
        return flight;
    }

    public Flight updateFlightStatus(String flightId, FlightStatus targetStatus) {
        requireLoggedInAdmin();
        ValidationException.requireNonNull(targetStatus, "Flight status");
        if (targetStatus == FlightStatus.ARRIVED) {
            return arriveFlight(flightId);
        }
        if (targetStatus == FlightStatus.CANCELLED) {
            return cancelFlight(flightId);
        }
        Flight flight = system.findFlightById(flightId);
        flight.updateStatus(targetStatus);
        return flight;
    }

    public Flight arriveFlight(String flightId) {
        requireLoggedInAdmin();
        system.arriveFlight(flightId);
        return system.findFlightById(flightId);
    }

    public Flight cancelFlight(String flightId) {
        requireLoggedInAdmin();
        system.cancelFlight(flightId);
        return system.findFlightById(flightId);
    }

    public List<Booking> getAllBookings() {
        requireLoggedInAdmin();
        return system.getBookings();
    }

    public List<Passenger> getAllPassengers() {
        requireLoggedInAdmin();
        return system.getPassengers();
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

    private Admin requireLoggedInAdmin() {
        if (!(currentUser instanceof Admin admin)) {
            throw new ValidationException("Admin login is required for this action");
        }
        return admin;
    }

    private static LocalDate parseIsoDate(String dateText, String fieldName) {
        String trimmed = ValidationException.requireNonBlank(dateText, fieldName);
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(fieldName + " must follow YYYY-MM-DD format", ex);
        }
    }

    private static LocalDateTime parseDateTime(String dateTimeText, String fieldName) {
        String trimmed = ValidationException.requireNonBlank(dateTimeText, fieldName);
        try {
            return LocalDateTime.parse(trimmed, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new ValidationException(fieldName + " must follow YYYY-MM-DD HH:MM format", ex);
        }
    }

    private static double parsePositiveDouble(String amountText, String fieldName) {
        String trimmed = ValidationException.requireNonBlank(amountText, fieldName);
        double parsed;
        try {
            parsed = Double.parseDouble(trimmed);
        } catch (NumberFormatException ex) {
            throw new ValidationException(fieldName + " must be a positive number", ex);
        }
        return ValidationException.requirePositive(parsed, fieldName);
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

    public List<Object[]> toAdminBookingTableRows(List<Booking> bookings) {
        ValidationException.requireNonNull(bookings, "Bookings");
        return bookings.stream()
                .map(booking -> new Object[]{
                        booking.getBookingId(),
                        formatPassengerSummary(booking.getPassenger()),
                        booking.getFlight().getFlightNumber(),
                        formatRoute(booking.getFlight()),
                        formatSeatNumbers(booking),
                        formatCurrency(booking.getTotalPrice()),
                        booking.getStatus(),
                        getBookingPaymentStatus(booking)
                })
                .toList();
    }

    public List<Object[]> toPassengerTableRows(List<Passenger> passengers) {
        ValidationException.requireNonNull(passengers, "Passengers");
        return passengers.stream()
                .map(passenger -> new Object[]{
                        passenger.getId(),
                        passenger.getFullName(),
                        passenger.getEmail(),
                        passenger.getPhoneNumber(),
                        DateTimeFormatter.ISO_LOCAL_DATE.format(passenger.getDateOfBirth()),
                        passenger.getGender(),
                        passenger.getPassportOrId(),
                        passenger.getBookingHistory().size()
                })
                .toList();
    }

    public static String formatPassengerSummary(Passenger passenger) {
        ValidationException.requireNonNull(passenger, "Passenger");
        return passenger.getId() + " · " + passenger.getFullName();
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        ValidationException.requireNonNull(dateTime, "Date time");
        return DATE_TIME_FORMATTER.format(dateTime);
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

    public AdminMetrics getAdminMetrics() {
        requireLoggedInAdmin();
        List<Booking> bookings = system.getBookings();
        double settledRevenue = bookings.stream()
                .map(Booking::getPayment)
                .filter(payment -> payment != null && payment.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
        return new AdminMetrics(
                system.getFlights().size(),
                bookings.size(),
                system.getPassengers().size(),
                settledRevenue
        );
    }

    /**
     * Live network metrics displayed in the Web3 login showcase panel.
     */
    public record PortalMetrics(int airportsCount, int scheduledFlightsCount, int availableSeatsCount) {
    }

    /**
     * Live operations metrics displayed in the Admin system-wide overview panel.
     */
    public record AdminMetrics(int totalFlightsCount,
                               int totalBookingsCount,
                               int registeredPassengersCount,
                               double settledRevenue) {
    }
}
