package com.airlinereservation.model;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Scheduled flight owning an isolated seat map derived from its operating aircraft.
 */
public class Flight {

    private static final int COLUMNS_PER_ROW = 4;
    private static final Map<FlightStatus, FlightStatus> NEXT_OPERATIONAL_STATUS = Map.of(
            FlightStatus.SCHEDULED, FlightStatus.BOARDING,
            FlightStatus.BOARDING, FlightStatus.DEPARTED,
            FlightStatus.DEPARTED, FlightStatus.ARRIVED
    );

    private final String flightId;
    private final String flightNumber;
    private final Airport departureAirport;
    private final Airport arrivalAirport;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final Aircraft aircraft;
    private final Map<String, Seat> seats;
    private final double basePrice;
    private FlightStatus status;

    public Flight(String flightId,
                  String flightNumber,
                  Airport departureAirport,
                  Airport arrivalAirport,
                  LocalDateTime departureTime,
                  LocalDateTime arrivalTime,
                  Aircraft aircraft,
                  double basePrice) {
        this(flightId, flightNumber, departureAirport, arrivalAirport, departureTime, arrivalTime,
                aircraft, basePrice, defaultBusinessSeatCount(
                        ValidationException.requireNonNull(aircraft, "Aircraft").getCapacity()));
    }

    public Flight(String flightId,
                  String flightNumber,
                  Airport departureAirport,
                  Airport arrivalAirport,
                  LocalDateTime departureTime,
                  LocalDateTime arrivalTime,
                  Aircraft aircraft,
                  double basePrice,
                  int businessSeatCount) {
        this.flightId = ValidationException.requireNonBlank(flightId, "Flight ID");
        this.flightNumber = ValidationException.requireNonBlank(flightNumber, "Flight number");
        this.departureAirport = ValidationException.requireNonNull(departureAirport, "Departure airport");
        this.arrivalAirport = ValidationException.requireNonNull(arrivalAirport, "Arrival airport");
        if (this.departureAirport.getAirportCode().equalsIgnoreCase(this.arrivalAirport.getAirportCode())) {
            throw new ValidationException("Departure and arrival airports must be different");
        }
        this.departureTime = ValidationException.requireNonNull(departureTime, "Departure time");
        this.arrivalTime = ValidationException.requireNonNull(arrivalTime, "Arrival time");
        if (!this.arrivalTime.isAfter(this.departureTime)) {
            throw new ValidationException("Arrival time must be after departure time");
        }
        this.aircraft = ValidationException.requireNonNull(aircraft, "Aircraft");
        int capacity = this.aircraft.getCapacity();
        this.basePrice = ValidationException.requirePositive(basePrice, "Base price");
        if (businessSeatCount < 0 || businessSeatCount > capacity) {
            throw new ValidationException("Business seat count must be between 0 and " + capacity);
        }
        this.status = FlightStatus.SCHEDULED;
        this.seats = generateSeatMap(capacity, businessSeatCount);
    }

    public String getFlightId() {
        return flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public List<Seat> getSeats() {
        return List.copyOf(seats.values());
    }

    public List<Seat> getAvailableSeats() {
        return seats.values().stream()
                .filter(Seat::isAvailable)
                .toList();
    }

    public Seat findSeat(String seatNumber) {
        String key = ValidationException.requireNonBlank(seatNumber, "Seat number").toUpperCase(Locale.ROOT);
        Seat seat = seats.get(key);
        if (seat == null) {
            throw new EntityNotFoundException("Seat not found on flight " + flightId + ": " + seatNumber.trim());
        }
        return seat;
    }

    public boolean hasAvailableSeats(int count) {
        if (count <= 0) {
            throw new ValidationException("Required seat count must be positive");
        }
        return getAvailableSeats().size() >= count;
    }

    public boolean matches(String depCode, String arrCode, LocalDate date) {
        if (status != FlightStatus.SCHEDULED || depCode == null || arrCode == null || date == null) {
            return false;
        }
        String normalizedDep = depCode.trim();
        String normalizedArr = arrCode.trim();
        if (normalizedDep.isEmpty() || normalizedArr.isEmpty()) {
            return false;
        }
        return departureAirport.getAirportCode().equalsIgnoreCase(normalizedDep)
                && arrivalAirport.getAirportCode().equalsIgnoreCase(normalizedArr)
                && departureTime.toLocalDate().equals(date);
    }

    public void updateStatus(FlightStatus newStatus) {
        ValidationException.requireNonNull(newStatus, "Flight status");
        if (newStatus == FlightStatus.CANCELLED) {
            cancel();
            return;
        }
        if (NEXT_OPERATIONAL_STATUS.get(status) != newStatus) {
            throw new InvalidStateException("Cannot transition flight " + flightId + " from " + status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public void cancel() {
        if (status != FlightStatus.SCHEDULED && status != FlightStatus.BOARDING) {
            throw new InvalidStateException("Cannot cancel flight " + flightId + " in status: " + status);
        }
        this.status = FlightStatus.CANCELLED;
    }

    private static int defaultBusinessSeatCount(int capacity) {
        return capacity >= 5 ? (int) Math.ceil(capacity * 0.2) : 0;
    }

    private static Map<String, Seat> generateSeatMap(int capacity, int businessSeatCount) {
        Map<String, Seat> seatMap = new LinkedHashMap<>();
        int firstEconomyRow = (businessSeatCount / COLUMNS_PER_ROW) + 1;
        for (int index = 0; index < capacity; index++) {
            int row = (index / COLUMNS_PER_ROW) + 1;
            char column = (char) ('A' + (index % COLUMNS_PER_ROW));
            String seatNumber = row + String.valueOf(column);
            Seat seat = index < businessSeatCount
                    ? new BusinessSeat(seatNumber)
                    : new EconomySeat(seatNumber, row == firstEconomyRow);
            seatMap.put(seatNumber, seat);
        }
        return seatMap;
    }
}
