package com.airlinereservation.system;

import com.airlinereservation.exception.DuplicateEntityException;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;

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

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
