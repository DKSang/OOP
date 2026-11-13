package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

/**
 * Standardized airport entity identified by its airport code.
 */
public class Airport {

    private final String airportCode;
    private final String airportName;
    private final String city;

    public Airport(String airportCode, String airportName, String city) {
        this.airportCode = ValidationException.requireNonBlank(airportCode, "Airport code");
        this.airportName = ValidationException.requireNonBlank(airportName, "Airport name");
        this.city = ValidationException.requireNonBlank(city, "City");
    }

    public String getAirportCode() {
        return airportCode;
    }

    public String getAirportName() {
        return airportName;
    }

    public String getCity() {
        return city;
    }
}
