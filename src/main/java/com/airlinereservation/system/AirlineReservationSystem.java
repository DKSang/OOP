package com.airlinereservation.system;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Flight;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central coordinator managing system catalogs and core domain workflows.
 */
public class AirlineReservationSystem {

    private final Map<String, Airport> airports = new LinkedHashMap<>();
    private final Map<String, Aircraft> aircrafts = new LinkedHashMap<>();
    private final Map<String, Flight> flights = new LinkedHashMap<>();

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

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
