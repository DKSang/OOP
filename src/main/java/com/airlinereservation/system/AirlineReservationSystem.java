package com.airlinereservation.system;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Seat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Central coordinator managing system catalogs and core domain workflows.
 */
public class AirlineReservationSystem {

    private final Clock clock;
    private final Map<String, Airport> airports = new LinkedHashMap<>();
    private final Map<String, Aircraft> aircrafts = new LinkedHashMap<>();
    private final Map<String, Flight> flights = new LinkedHashMap<>();
    private final Map<String, Passenger> passengers = new LinkedHashMap<>();
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private long bookingSequence;

    public AirlineReservationSystem() {
        this(Clock.systemDefaultZone());
    }

    public AirlineReservationSystem(Clock clock) {
        this.clock = ValidationException.requireNonNull(clock, "Clock");
    }

    public void addAirport(Airport airport) {
        ValidationException.requireNonNull(airport, "Airport");
        String key = normalizeCode(airport.getAirportCode());
        if (airports.containsKey(key)) {
            throw new DuplicateEntityException("Airport already exists with code: " + airport.getAirportCode());
        }
        airports.put(key, airport);
    }

    public Airport findAirportByCode(String airportCode) {
        String trimmed = ValidationException.requireNonBlank(airportCode, "Airport code");
        Airport airport = airports.get(normalizeCode(trimmed));
        if (airport == null) {
            throw new EntityNotFoundException("Airport not found with code: " + trimmed);
        }
        return airport;
    }

    public List<Airport> getAirports() {
        return List.copyOf(airports.values());
    }

    public void addAircraft(Aircraft aircraft) {
        ValidationException.requireNonNull(aircraft, "Aircraft");
        String key = aircraft.getAircraftId();
        if (aircrafts.containsKey(key)) {
            throw new DuplicateEntityException("Aircraft already exists with ID: " + key);
        }
        aircrafts.put(key, aircraft);
    }

    public Aircraft findAircraftById(String aircraftId) {
        String key = ValidationException.requireNonBlank(aircraftId, "Aircraft ID");
        Aircraft aircraft = aircrafts.get(key);
        if (aircraft == null) {
            throw new EntityNotFoundException("Aircraft not found with ID: " + key);
        }
        return aircraft;
    }

    public List<Aircraft> getAircrafts() {
        return List.copyOf(aircrafts.values());
    }

    public void addFlight(Flight flight) {
        ValidationException.requireNonNull(flight, "Flight");
        String key = flight.getFlightId();
        if (flights.containsKey(key)) {
            throw new DuplicateEntityException("Flight already exists with ID: " + key);
        }
        Airport registeredDeparture = findAirportByCode(flight.getDepartureAirport().getAirportCode());
        if (registeredDeparture != flight.getDepartureAirport()) {
            throw new ValidationException("Flight departure airport must be registered in the system");
        }
        Airport registeredArrival = findAirportByCode(flight.getArrivalAirport().getAirportCode());
        if (registeredArrival != flight.getArrivalAirport()) {
            throw new ValidationException("Flight arrival airport must be registered in the system");
        }
        Aircraft registeredAircraft = findAircraftById(flight.getAircraft().getAircraftId());
        if (registeredAircraft != flight.getAircraft()) {
            throw new ValidationException("Flight aircraft must be registered in the system");
        }
        flights.put(key, flight);
    }

    public Flight findFlightById(String flightId) {
        String key = ValidationException.requireNonBlank(flightId, "Flight ID");
        Flight flight = flights.get(key);
        if (flight == null) {
            throw new EntityNotFoundException("Flight not found with ID: " + key);
        }
        return flight;
    }

    public List<Flight> getFlights() {
        return List.copyOf(flights.values());
    }

    public List<Flight> searchFlights(String depCode, String arrCode, LocalDate date, int seatCount) {
        String trimmedDep = ValidationException.requireNonBlank(depCode, "Departure airport code");
        String trimmedArr = ValidationException.requireNonBlank(arrCode, "Arrival airport code");
        ValidationException.requireNonNull(date, "Departure date");
        if (seatCount <= 0) {
            throw new ValidationException("Seat count must be positive");
        }
        return flights.values().stream()
                .filter(flight -> flight.matches(trimmedDep, trimmedArr, date)
                        && flight.hasAvailableSeats(seatCount))
                .toList();
    }

    public void registerPassenger(Passenger passenger) {
        ValidationException.requireNonNull(passenger, "Passenger");
        String key = passenger.getId();
        if (passengers.containsKey(key)) {
            throw new DuplicateEntityException("Passenger already exists with ID: " + key);
        }
        boolean duplicatePassport = passengers.values().stream()
                .anyMatch(existing -> existing.getPassportOrId().equalsIgnoreCase(passenger.getPassportOrId()));
        if (duplicatePassport) {
            throw new DuplicateEntityException("Passenger already exists with passport/ID: " + passenger.getPassportOrId());
        }
        passengers.put(key, passenger);
    }

    public Passenger findPassengerById(String passengerId) {
        return findById(passengers, passengerId, "Passenger");
    }

    public List<Passenger> getPassengers() {
        return List.copyOf(passengers.values());
    }

    public Booking createBooking(String passengerId, String flightId, List<String> seatNumbers) {
        Passenger passenger = findPassengerById(passengerId);
        Flight flight = findFlightById(flightId);
        requireScheduledFlight(flight);
        List<Seat> seats = resolveSeatNumbers(flight, seatNumbers);
        return createValidatedBooking(passenger, flight, seats);
    }

    public Booking createBooking(Passenger passenger, Flight flight, List<Seat> seats) {
        requireRegisteredPassenger(passenger);
        requireRegisteredFlight(flight);
        requireScheduledFlight(flight);
        List<Seat> validatedSeats = Booking.copyValidatedSeats(seats);
        return createValidatedBooking(passenger, flight, validatedSeats);
    }

    public Booking findBookingById(String bookingId) {
        return findById(bookings, bookingId, "Booking");
    }

    public List<Booking> getBookings() {
        return List.copyOf(bookings.values());
    }

    private Booking createValidatedBooking(Passenger passenger, Flight flight, List<Seat> seats) {
        for (Seat seat : seats) {
            if (!flight.containsSeat(seat)) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber()
                        + " does not belong to flight " + flight.getFlightId());
            }
            if (!seat.isAvailable()) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber()
                        + " is not available (status: " + seat.getStatus() + ")");
            }
        }
        for (Seat seat : seats) {
            seat.reserve();
        }
        String bookingId = "BK-" + (++bookingSequence);
        Booking booking = new Booking(bookingId, flight, passenger, seats, LocalDateTime.now(clock));
        bookings.put(bookingId, booking);
        passenger.addBooking(booking);
        return booking;
    }

    private static <T> T findById(Map<String, T> registry, String id, String entityName) {
        String key = ValidationException.requireNonBlank(id, entityName + " ID");
        T entity = registry.get(key);
        if (entity == null) {
            throw new EntityNotFoundException(entityName + " not found with ID: " + key);
        }
        return entity;
    }

    private void requireRegisteredPassenger(Passenger passenger) {
        ValidationException.requireNonNull(passenger, "Passenger");
        Passenger registered = findPassengerById(passenger.getId());
        if (registered != passenger) {
            throw new ValidationException("Passenger must be registered in the system");
        }
    }

    private void requireRegisteredFlight(Flight flight) {
        ValidationException.requireNonNull(flight, "Flight");
        Flight registered = findFlightById(flight.getFlightId());
        if (registered != flight) {
            throw new ValidationException("Flight must be registered in the system");
        }
    }

    private static void requireScheduledFlight(Flight flight) {
        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new InvalidStateException("Cannot create booking for flight " + flight.getFlightId()
                    + " in status: " + flight.getStatus());
        }
    }

    private static List<Seat> resolveSeatNumbers(Flight flight, List<String> seatNumbers) {
        ValidationException.requireNonNull(seatNumbers, "Seat numbers");
        if (seatNumbers.isEmpty()) {
            throw new ValidationException("Seat numbers must not be empty");
        }
        Set<String> seenNumbers = new HashSet<>();
        List<Seat> resolvedSeats = new ArrayList<>(seatNumbers.size());
        for (String seatNumber : seatNumbers) {
            String normalized = normalizeCode(ValidationException.requireNonBlank(seatNumber, "Seat number"));
            if (!seenNumbers.add(normalized)) {
                throw new ValidationException("Seat numbers must not contain duplicates: " + normalized);
            }
            resolvedSeats.add(flight.findSeat(normalized));
        }
        return resolvedSeats;
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
