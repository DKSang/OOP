package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.PaymentException;
import com.airlinereservation.exception.SeatNotAvailableException;
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
    private BookingState state;
    private final LocalDateTime createdAt;

    public Booking(String bookingId,
                   Flight flight,
                   Passenger passenger,
                   List<Seat> selectedSeats,
                   LocalDateTime createdAt) {
        this(
                ValidationException.requireNonBlank(bookingId, "Booking ID"),
                ValidationException.requireNonNull(flight, "Flight"),
                ValidationException.requireNonNull(passenger, "Passenger"),
                copyValidatedSeats(selectedSeats),
                ValidationException.requireNonNull(createdAt, "Created timestamp"),
                PendingBookingState.INSTANCE
        );
    }

    private Booking(String bookingId,
                    Flight flight,
                    Passenger passenger,
                    List<Seat> validatedSeats,
                    LocalDateTime createdAt,
                    BookingState initialState) {
        this.bookingId = bookingId;
        this.flight = flight;
        this.passenger = passenger;
        this.selectedSeats = validatedSeats;
        this.createdAt = createdAt;
        this.state = initialState;
        this.totalPrice = calculateTotalPrice();
    }

    /**
     * Atomically validates flight readiness and seat availability, reserves seats,
     * creates a pending booking, and records it in the passenger's history.
     */
    public static Booking createReserved(String bookingId,
                                         Flight flight,
                                         Passenger passenger,
                                         List<Seat> selectedSeats,
                                         LocalDateTime createdAt) {
        String validBookingId = ValidationException.requireNonBlank(bookingId, "Booking ID");
        Flight validFlight = ValidationException.requireNonNull(flight, "Flight");
        Passenger validPassenger = ValidationException.requireNonNull(passenger, "Passenger");
        List<Seat> validatedSeats = copyValidatedSeats(selectedSeats);
        LocalDateTime validCreatedAt = ValidationException.requireNonNull(createdAt, "Created timestamp");

        if (!validFlight.canAcceptNewBooking()) {
            throw new InvalidStateException("Cannot create booking for flight " + validFlight.getFlightId()
                    + " in status: " + validFlight.getStatus());
        }
        for (Seat seat : validatedSeats) {
            if (!validFlight.containsSeat(seat)) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber()
                        + " does not belong to flight " + validFlight.getFlightId());
            }
            if (!seat.isAvailable()) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber()
                        + " is not available (status: " + seat.getStatus() + ")");
            }
        }
        for (Seat seat : validatedSeats) {
            seat.reserve();
        }
        Booking booking = new Booking(
                validBookingId, validFlight, validPassenger, validatedSeats, validCreatedAt, PendingBookingState.INSTANCE);
        validPassenger.addBooking(booking);
        return booking;
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
        return state.getStatus();
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
        state.validateConfirmable(this);
    }

    public void confirmBooking(Payment payment) {
        ValidationException.requireNonNull(payment, "Payment");
        state.confirmBooking(this, payment);
    }

    /**
     * Checks in a confirmed booking while its flight is SCHEDULED or BOARDING.
     */
    public void checkIn() {
        state.checkIn(this);
    }

    /**
     * Completes a checked-in booking once its flight has arrived.
     */
    public void complete() {
        state.complete(this);
    }

    /**
     * Cancels a pending or confirmed booking before departure, releasing seats and refunding payment.
     */
    public boolean cancelBooking() {
        return state.cancelBooking(this);
    }

    /**
     * Cancels an unpaid pending booking when its flight arrives, releasing its reserved seats.
     */
    public void cancelPendingOnArrival() {
        state.cancelPendingOnArrival(this);
    }

    /**
     * Handles cancellation of the associated flight by cancelling open bookings,
     * releasing seats, and refunding completed payments.
     */
    public void onFlightCancelled() {
        state.onFlightCancelled(this);
    }

    /**
     * Handles arrival of the associated flight, completing checked-in bookings
     * and expiring unpaid pending bookings while preserving terminal and no-show states.
     */
    public void onFlightArrived() {
        state.onFlightArrived(this);
    }

    void transitionTo(BookingState nextState) {
        this.state = nextState;
    }

    InvalidStateException invalidTransition(String action) {
        return new InvalidStateException("Cannot " + action + " booking " + bookingId + " in status: " + getStatus());
    }

    void requireReservedSeats(String action) {
        for (Seat seat : selectedSeats) {
            if (seat.getStatus() != SeatStatus.RESERVED) {
                throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                        + "; seat " + seat.getSeatNumber() + " is in status: " + seat.getStatus());
            }
        }
    }

    void verifyConfirmablePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot confirm booking " + bookingId
                    + " with payment " + payment.getPaymentId() + " in status: " + payment.getStatus());
        }
        if (Math.abs(payment.getAmount() - totalPrice) > PRICE_MATCH_TOLERANCE) {
            throw new PaymentException("Payment amount " + payment.getAmount()
                    + " does not match booking total " + totalPrice);
        }
    }

    void occupySeats() {
        for (Seat seat : selectedSeats) {
            seat.occupy();
        }
    }

    void attachPayment(Payment payment) {
        this.payment = payment;
    }

    void refundPaymentIfPresent() {
        if (payment != null) {
            payment.refund();
        }
    }

    void requireHeldSeats(String action) {
        for (Seat seat : selectedSeats) {
            if (seat.isAvailable()) {
                throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                        + "; seat " + seat.getSeatNumber() + " is already available");
            }
        }
    }

    void releaseSeats() {
        for (Seat seat : selectedSeats) {
            seat.release();
        }
    }

    void completeCancellation() {
        requireHeldSeats("cancel");
        refundPaymentIfPresent();
        releaseSeats();
        transitionTo(CancelledBookingState.INSTANCE);
    }

    void cancelOnFlightCancellation() {
        requireCancelledFlight("cancel");
        completeCancellation();
    }

    void requireArrivedFlight(String action) {
        if (!flight.hasArrived()) {
            throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                    + " for flight " + flight.getFlightId() + " in status: " + flight.getStatus());
        }
    }

    void requireActiveFlight(String action) {
        if (!flight.isActiveForBooking()) {
            throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                    + " for flight " + flight.getFlightId() + " in status: " + flight.getStatus());
        }
    }

    void requireCancellableFlight(String action) {
        if (!flight.canCancelBooking()) {
            throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                    + " for flight " + flight.getFlightId() + " in status: " + flight.getStatus());
        }
    }

    void requireCancelledFlight(String action) {
        if (!flight.isCancelled()) {
            throw new InvalidStateException("Cannot " + action + " booking " + bookingId
                    + " because flight " + flight.getFlightId() + " is in status: " + flight.getStatus());
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
