package com.airlinereservation.gui;

import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import com.airlinereservation.model.Seat;
import com.airlinereservation.system.AirlineReservationSystem;
import com.airlinereservation.system.DemoDataSeeder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Passive-View controller mediating between Swing views and {@link AirlineReservationSystem}.
 */
public class ReservationGuiController {

    public static final String[] FLIGHT_TABLE_COLUMNS = {
            "Flight ID", "Flight No", "Route", "Departure", "Arrival",
            "Aircraft", "Available Seats", "Base Price", "Status"
    };

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("Passenger - PAX-01 (Nguyen Van An)", "PAX-01", Person.DEFAULT_PASSWORD, "Passenger"),
            new DemoAccount("Passenger - PAX-02 (Tran Thi Binh)", "PAX-02", Person.DEFAULT_PASSWORD, "Passenger"),
            new DemoAccount("Admin - ADM-01 (Le Minh Cuong)", "ADM-01", DemoDataSeeder.DEMO_ADMIN_PASSWORD, "Admin")
    );

    private final AirlineReservationSystem system;
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
                        flight.getDepartureAirport().getAirportCode() + " -> " + flight.getArrivalAirport().getAirportCode(),
                        DATE_TIME_FORMATTER.format(flight.getDepartureTime()),
                        DATE_TIME_FORMATTER.format(flight.getArrivalTime()),
                        flight.getAircraft().getModel(),
                        flight.getAvailableSeats().size(),
                        formatCurrency(flight.getBasePrice()),
                        flight.getStatus()
                })
                .toList();
    }

    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f VND", amount);
    }
}
