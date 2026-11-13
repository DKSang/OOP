package com.airlinereservation.model;

import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;

/**
 * Abstract flight seat with flight-isolated status and polymorphic fare calculation.
 */
public abstract class Seat {

    protected final String seatNumber;
    protected SeatStatus status;

    protected Seat(String seatNumber) {
        this.seatNumber = ValidationException.requireNonBlank(seatNumber, "Seat number");
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void reserve() {
        if (!isAvailable()) {
            throw new SeatNotAvailableException("Seat " + seatNumber + " is not available (status: " + status + ")");
        }
        status = SeatStatus.RESERVED;
    }

    public void occupy() {
        if (status != SeatStatus.RESERVED) {
            throw new InvalidStateException("Cannot occupy seat " + seatNumber + " from status: " + status);
        }
        status = SeatStatus.OCCUPIED;
    }

    public void release() {
        if (status == SeatStatus.AVAILABLE) {
            throw new InvalidStateException("Seat " + seatNumber + " is already available");
        }
        status = SeatStatus.AVAILABLE;
    }

    public abstract double calculatePrice(double basePrice);

    public abstract String getSeatType();

    protected static double requireValidBasePrice(double basePrice) {
        return ValidationException.requirePositive(basePrice, "Base price");
    }
}
