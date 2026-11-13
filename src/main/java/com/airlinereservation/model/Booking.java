package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reservation record associating a passenger, a flight, selected seats, and calculated fare.
 */
public class Booking {

    private final String bookingId;
    private final Flight flight;
    private final Passenger passenger;
    private final List<Seat> selectedSeats;
    private final double totalPrice;
    private BookingStatus status;
    private final LocalDateTime createdAt;

    public Booking(String bookingId,
                   Flight flight,
                   Passenger passenger,
                   List<Seat> selectedSeats,
                   LocalDateTime createdAt) {
        this.bookingId = ValidationException.requireNonBlank(bookingId, "Booking ID");
        this.flight = ValidationException.requireNonNull(flight, "Flight");
        this.passenger = ValidationException.requireNonNull(passenger, "Passenger");
        this.selectedSeats = copyValidatedSeats(selectedSeats);
        this.createdAt = ValidationException.requireNonNull(createdAt, "Created timestamp");
        this.status = BookingStatus.PENDING;
        this.totalPrice = calculateTotalPrice();
    }

    public String getBookingId() {
        return bookingId;
    }

    public Flight getFlight() {
        return flight;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double calculateTotalPrice() {
        double basePrice = flight.getBasePrice();
        return selectedSeats.stream()
                .mapToDouble(seat -> seat.calculatePrice(basePrice))
                .sum();
    }

    public static List<Seat> copyValidatedSeats(List<Seat> seats) {
        ValidationException.requireNonNull(seats, "Selected seats");
        if (seats.isEmpty()) {
            throw new ValidationException("Selected seats must not be empty");
        }
        Set<Seat> seen = new HashSet<>();
        for (Seat seat : seats) {
            ValidationException.requireNonNull(seat, "Selected seat");
            if (!seen.add(seat)) {
                throw new ValidationException("Selected seats must not contain duplicates: " + seat.getSeatNumber());
            }
        }
        return List.copyOf(seats);
    }
}
