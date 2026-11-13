package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.PaymentException;
import com.airlinereservation.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reservation record associating a passenger, a flight, selected seats, and calculated fare.
 */
public class Booking {

    private static final double PRICE_MATCH_TOLERANCE = 1e-6;

    private final String bookingId;
    private final Flight flight;
    private final Passenger passenger;
    private final List<Seat> selectedSeats;
    private Payment payment;
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

    public Payment getPayment() {
        return payment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Verifies that the booking, its flight, and all selected seats can transition to CONFIRMED.
     */
    public void validateConfirmable() {
        if (status != BookingStatus.PENDING) {
            throw new InvalidStateException("Cannot confirm booking " + bookingId + " in status: " + status);
        }
        requireActiveFlight("confirm");
        for (Seat seat : selectedSeats) {
            if (seat.getStatus() != SeatStatus.RESERVED) {
                throw new InvalidStateException("Cannot confirm booking " + bookingId
                        + "; seat " + seat.getSeatNumber() + " is in status: " + seat.getStatus());
            }
        }
    }

    public void confirmBooking(Payment payment) {
        ValidationException.requireNonNull(payment, "Payment");
        validateConfirmable();
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot confirm booking " + bookingId
                    + " with payment " + payment.getPaymentId() + " in status: " + payment.getStatus());
        }
        if (Math.abs(payment.getAmount() - totalPrice) > PRICE_MATCH_TOLERANCE) {
            throw new PaymentException("Payment amount " + payment.getAmount()
                    + " does not match booking total " + totalPrice);
        }
        for (Seat seat : selectedSeats) {
            seat.occupy();
        }
        this.payment = payment;
        this.status = BookingStatus.CONFIRMED;
    }

    /**
     * Checks in a confirmed booking while its flight is SCHEDULED or BOARDING.
     */
    public void checkIn() {
        if (status != BookingStatus.CONFIRMED) {
            throw new InvalidStateException("Cannot check in booking " + bookingId + " in status: " + status);
        }
        requireActiveFlight("check in");
        this.status = BookingStatus.CHECKED_IN;
    }

    /**
     * Cancels a pending or confirmed booking before departure, releasing seats and refunding payment.
     */
    public boolean cancelBooking() {
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new InvalidStateException("Cannot cancel booking " + bookingId + " in status: " + status);
        }
        FlightStatus flightStatus = flight.getStatus();
        if (flightStatus == FlightStatus.DEPARTED || flightStatus == FlightStatus.ARRIVED) {
            throw new InvalidStateException("Cannot cancel booking " + bookingId
                    + " for flight " + flight.getFlightId() + " in status: " + flightStatus);
        }
        for (Seat seat : selectedSeats) {
            if (seat.isAvailable()) {
                throw new InvalidStateException("Cannot cancel booking " + bookingId
                        + "; seat " + seat.getSeatNumber() + " is already available");
            }
        }
        if (payment != null) {
            payment.refund();
        }
        for (Seat seat : selectedSeats) {
            seat.release();
        }
        this.status = BookingStatus.CANCELLED;
        return true;
    }

    private void requireActiveFlight(String action) {
        FlightStatus flightStatus = flight.getStatus();
        if (flightStatus != FlightStatus.SCHEDULED && flightStatus != FlightStatus.BOARDING) {
            throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                    + " for flight " + flight.getFlightId() + " in status: " + flightStatus);
        }
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
