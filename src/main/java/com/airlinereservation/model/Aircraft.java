package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

/**
 * Physical aircraft entity with static capacity configuration.
 */
public class Aircraft {

    private final String aircraftId;
    private final String model;
    private final String manufacturer;
    private final int seatCapacity;

    public Aircraft(String aircraftId, String model, String manufacturer, int seatCapacity) {
        this.aircraftId = ValidationException.requireNonBlank(aircraftId, "Aircraft ID");
        this.model = ValidationException.requireNonBlank(model, "Model");
        this.manufacturer = ValidationException.requireNonBlank(manufacturer, "Manufacturer");
        if (seatCapacity <= 0) {
            throw new ValidationException("Seat capacity must be positive");
        }
        this.seatCapacity = seatCapacity;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getCapacity() {
        return seatCapacity;
    }

    public String getModelInfo() {
        return manufacturer + " " + model + " (" + seatCapacity + " seats)";
    }
}
