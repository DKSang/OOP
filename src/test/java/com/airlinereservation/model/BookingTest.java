package com.airlinereservation.model;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.PaymentException;
import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingTest {

    @Test
    void createsPendingBookingCalculatesPolymorphicTotalAndProtectsSeatList() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Seat businessSeat = flight.findSeat("1A"); // 2.0 * 1_000_000 = 2_000_000
        Seat extraLegroomEconomy = flight.findSeat("2A"); // 1.15 * 1_000_000 = 1_150_000
        Seat standardEconomy = flight.findSeat("3A"); // 1.0 * 1_000_000 = 1_000_000
        List<Seat> mutableInput = new ArrayList<>(List.of(businessSeat, extraLegroomEconomy, standardEconomy));
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 13, 10, 30);

        Booking booking = new Booking(" BK-01 ", flight, passenger, mutableInput, createdAt);
        mutableInput.clear();

        assertEquals("BK-01", booking.getBookingId());
        assertSame(flight, booking.getFlight());
        assertSame(passenger, booking.getPassenger());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(createdAt, booking.getCreatedAt());
        assertEquals(4_150_000.0, booking.calculateTotalPrice(), 0.001);
        assertEquals(4_150_000.0, booking.getTotalPrice(), 0.001);
        assertEquals(List.of(businessSeat, extraLegroomEconomy, standardEconomy), booking.getSelectedSeats());
        assertEquals(List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.COMPLETED,
                BookingStatus.CANCELLED
        ), List.of(BookingStatus.values()));

        assertThrows(UnsupportedOperationException.class, () -> booking.getSelectedSeats().add(businessSeat));
    }

    @Test
    void rejectsInvalidBookingArguments() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 4);
        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000
        );
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Seat seat = flight.findSeat("1A");
        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);

        assertThrows(ValidationException.class, () -> new Booking(" ", flight, passenger, List.of(seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", null, passenger, List.of(seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, null, List.of(seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, null, now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, List.of(), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, List.of(seat, seat), now));
        assertThrows(ValidationException.class, () -> new Booking("BK-1", flight, passenger, List.of(seat), null));
    }

    @Test
    void confirmsPendingBookingWithCompletedMatchingPaymentAndOccupiesSeats() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Seat seat1A = flight.findSeat("1A"); // 2_000_000
        Seat seat2A = flight.findSeat("2A"); // 1_150_000
        seat1A.reserve();
        seat2A.reserve();

        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);
        Booking booking = new Booking("BK-01", flight, passenger, List.of(seat1A, seat2A), now);
        assertNull(booking.getPayment());

        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-13T16:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        Payment payment = new Payment("PAY-01", 3_150_000.0, PaymentMethod.CREDIT_DEBIT_CARD, fixedClock);
        payment.processPayment(amount -> true);

        booking.confirmBooking(payment);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertSame(payment, booking.getPayment());
        assertEquals(SeatStatus.OCCUPIED, seat1A.getStatus());
        assertEquals(SeatStatus.OCCUPIED, seat2A.getStatus());
    }

    @Test
    void rejectsConfirmBookingOnInvalidPaymentOrLifecycleStateWithoutMutatingSeats() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Seat seat1A = flight.findSeat("1A");
        Seat seat2A = flight.findSeat("2A");
        seat1A.reserve();

        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);
        Booking unreservedSecondSeatBooking = new Booking("BK-00", flight, passenger, List.of(seat1A, seat2A), now);
        Payment matchingPayment = new Payment("PAY-00", 3_150_000.0, PaymentMethod.CASH);
        matchingPayment.processPayment(amount -> true);
        assertThrows(InvalidStateException.class, () -> unreservedSecondSeatBooking.confirmBooking(matchingPayment));
        assertEquals(SeatStatus.RESERVED, seat1A.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat2A.getStatus());

        seat2A.reserve();
        Booking booking = new Booking("BK-01", flight, passenger, List.of(seat1A, seat2A), now);

        assertThrows(ValidationException.class, () -> booking.confirmBooking(null));

        Payment pendingPayment = new Payment("PAY-01", 3_150_000.0, PaymentMethod.CASH);
        assertThrows(PaymentException.class, () -> booking.confirmBooking(pendingPayment));

        Payment failedPayment = new Payment("PAY-02", 3_150_000.0, PaymentMethod.CASH);
        failedPayment.processPayment(amount -> false);
        assertThrows(PaymentException.class, () -> booking.confirmBooking(failedPayment));

        Payment wrongAmountPayment = new Payment("PAY-03", 3_000_000.0, PaymentMethod.CASH);
        wrongAmountPayment.processPayment(amount -> true);
        assertThrows(PaymentException.class, () -> booking.confirmBooking(wrongAmountPayment));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNull(booking.getPayment());
        assertEquals(SeatStatus.RESERVED, seat1A.getStatus());
        assertEquals(SeatStatus.RESERVED, seat2A.getStatus());

        flight.cancel();
        assertThrows(InvalidStateException.class, () -> booking.confirmBooking(matchingPayment));
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(SeatStatus.RESERVED, seat1A.getStatus());

        // Also reject already confirmed booking
        Flight activeFlight = new Flight(
                "FL-02", "VN102", han, sgn,
                LocalDateTime.of(2026, 12, 10, 10, 0),
                LocalDateTime.of(2026, 12, 10, 12, 15),
                a320, 1_000_000, 4
        );
        Seat activeSeat = activeFlight.findSeat("1A");
        activeSeat.reserve();
        Booking confirmedBooking = new Booking("BK-02", activeFlight, passenger, List.of(activeSeat), now);
        Payment validForActive = new Payment("PAY-04", 2_000_000.0, PaymentMethod.BANK_TRANSFER);
        validForActive.processPayment(amount -> true);
        confirmedBooking.confirmBooking(validForActive);
        assertThrows(InvalidStateException.class, () -> confirmedBooking.confirmBooking(validForActive));
    }

    @Test
    void checksInConfirmedBookingWhenFlightIsScheduledOrBoardingAndRejectsInvalidStates() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );

        Flight flight1 = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Seat seat1A = flight1.findSeat("1A");
        Seat seat1B = flight1.findSeat("1B");
        Seat seat1C = flight1.findSeat("1C");
        seat1A.reserve();
        seat1B.reserve();
        seat1C.reserve();

        Booking pendingBooking = new Booking("BK-P", flight1, passenger, List.of(seat1A), now);
        assertThrows(InvalidStateException.class, pendingBooking::checkIn);
        assertEquals(BookingStatus.PENDING, pendingBooking.getStatus());

        Booking scheduledCheckIn = new Booking("BK-1", flight1, passenger, List.of(seat1B), now);
        Payment pay1 = new Payment("PAY-1", 2_000_000.0, PaymentMethod.CASH);
        pay1.processPayment(amount -> true);
        scheduledCheckIn.confirmBooking(pay1);

        scheduledCheckIn.checkIn();
        assertEquals(BookingStatus.CHECKED_IN, scheduledCheckIn.getStatus());
        assertThrows(InvalidStateException.class, scheduledCheckIn::checkIn);

        Booking boardingCheckIn = new Booking("BK-2", flight1, passenger, List.of(seat1C), now);
        Payment pay2 = new Payment("PAY-2", 2_000_000.0, PaymentMethod.CASH);
        pay2.processPayment(amount -> true);
        boardingCheckIn.confirmBooking(pay2);
        flight1.updateStatus(FlightStatus.BOARDING);

        boardingCheckIn.checkIn();
        assertEquals(BookingStatus.CHECKED_IN, boardingCheckIn.getStatus());

        // Confirmed booking cannot check in once flight is DEPARTED, ARRIVED, or CANCELLED
        Seat seat1D = flight1.findSeat("1D");
        seat1D.reserve();
        Booking departedFlightBooking = new Booking("BK-3", flight1, passenger, List.of(seat1D), now);
        Payment pay3 = new Payment("PAY-3", 2_000_000.0, PaymentMethod.CASH);
        pay3.processPayment(amount -> true);
        departedFlightBooking.confirmBooking(pay3);

        flight1.updateStatus(FlightStatus.DEPARTED);
        assertThrows(InvalidStateException.class, departedFlightBooking::checkIn);
        assertEquals(BookingStatus.CONFIRMED, departedFlightBooking.getStatus());

        flight1.updateStatus(FlightStatus.ARRIVED);
        assertThrows(InvalidStateException.class, departedFlightBooking::checkIn);
        assertEquals(BookingStatus.CONFIRMED, departedFlightBooking.getStatus());

        Flight cancelledFlight = new Flight(
                "FL-02", "VN102", han, sgn,
                LocalDateTime.of(2026, 12, 11, 7, 0),
                LocalDateTime.of(2026, 12, 11, 9, 15),
                a320, 1_000_000, 4
        );
        Seat cancelledSeat = cancelledFlight.findSeat("1A");
        cancelledSeat.reserve();
        Booking cancelledFlightBooking = new Booking("BK-4", cancelledFlight, passenger, List.of(cancelledSeat), now);
        Payment pay4 = new Payment("PAY-4", 2_000_000.0, PaymentMethod.CASH);
        pay4.processPayment(amount -> true);
        cancelledFlightBooking.confirmBooking(pay4);
        cancelledFlight.cancel();

        assertThrows(InvalidStateException.class, cancelledFlightBooking::checkIn);
        assertEquals(BookingStatus.CONFIRMED, cancelledFlightBooking.getStatus());
    }

    @Test
    void cancelsPendingAndConfirmedBookingsReleasesSeatsAndRefundsPayment() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );

        Flight flight1 = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Seat seat1A = flight1.findSeat("1A");
        Seat seat2A = flight1.findSeat("2A");
        seat1A.reserve();
        seat2A.reserve();

        Booking pendingBooking = new Booking("BK-P", flight1, passenger, List.of(seat1A, seat2A), now);
        assertTrue(pendingBooking.cancelBooking());
        assertEquals(BookingStatus.CANCELLED, pendingBooking.getStatus());
        assertNull(pendingBooking.getPayment());
        assertEquals(SeatStatus.AVAILABLE, seat1A.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat2A.getStatus());
        assertThrows(InvalidStateException.class, pendingBooking::checkIn);

        Seat seat1B = flight1.findSeat("1B");
        Seat seat2B = flight1.findSeat("2B");
        seat1B.reserve();
        seat2B.reserve();
        Booking confirmedBooking = new Booking("BK-C", flight1, passenger, List.of(seat1B, seat2B), now);
        Payment payment = new Payment("PAY-1", 3_150_000.0, PaymentMethod.CREDIT_DEBIT_CARD);
        payment.processPayment(amount -> true);
        confirmedBooking.confirmBooking(payment);

        flight1.updateStatus(FlightStatus.BOARDING);
        assertTrue(confirmedBooking.cancelBooking());
        assertEquals(BookingStatus.CANCELLED, confirmedBooking.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat1B.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat2B.getStatus());

        Flight cancelledFlight = new Flight(
                "FL-02", "VN102", han, sgn,
                LocalDateTime.of(2026, 12, 11, 7, 0),
                LocalDateTime.of(2026, 12, 11, 9, 15),
                a320, 1_000_000, 4
        );
        Seat seat1C = cancelledFlight.findSeat("1C");
        seat1C.reserve();
        Booking onCancelledFlight = new Booking("BK-F", cancelledFlight, passenger, List.of(seat1C), now);
        Payment payment2 = new Payment("PAY-2", 2_000_000.0, PaymentMethod.CASH);
        payment2.processPayment(amount -> true);
        onCancelledFlight.confirmBooking(payment2);
        cancelledFlight.cancel();

        assertTrue(onCancelledFlight.cancelBooking());
        assertEquals(BookingStatus.CANCELLED, onCancelledFlight.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment2.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat1C.getStatus());
    }

    @Test
    void rejectsCancellationForCheckedInCancelledOrDepartedArrivedBookingsWithoutMutatingState() {
        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 10, 30);
        Passenger passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );

        Flight flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        Seat seat1A = flight.findSeat("1A");
        Seat seat1B = flight.findSeat("1B");
        Seat seat1C = flight.findSeat("1C");
        Seat seat1D = flight.findSeat("1D");
        seat1A.reserve();
        seat1B.reserve();
        seat1C.reserve();
        seat1D.reserve();

        Booking checkedInBooking = new Booking("BK-CI", flight, passenger, List.of(seat1A), now);
        Payment payCi = new Payment("PAY-CI", 2_000_000.0, PaymentMethod.CASH);
        payCi.processPayment(amount -> true);
        checkedInBooking.confirmBooking(payCi);
        checkedInBooking.checkIn();

        assertThrows(InvalidStateException.class, checkedInBooking::cancelBooking);
        assertEquals(BookingStatus.CHECKED_IN, checkedInBooking.getStatus());
        assertEquals(PaymentStatus.COMPLETED, payCi.getStatus());
        assertEquals(SeatStatus.OCCUPIED, seat1A.getStatus());

        Booking alreadyCancelled = new Booking("BK-CAN", flight, passenger, List.of(seat1B), now);
        alreadyCancelled.cancelBooking();
        assertThrows(InvalidStateException.class, alreadyCancelled::cancelBooking);
        assertEquals(BookingStatus.CANCELLED, alreadyCancelled.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat1B.getStatus());

        Booking pendingOnDepartingFlight = new Booking("BK-DEP-P", flight, passenger, List.of(seat1C), now);
        Booking confirmedOnDepartingFlight = new Booking("BK-DEP-C", flight, passenger, List.of(seat1D), now);
        Payment payDep = new Payment("PAY-DEP", 2_000_000.0, PaymentMethod.E_WALLET);
        payDep.processPayment(amount -> true);
        confirmedOnDepartingFlight.confirmBooking(payDep);

        flight.updateStatus(FlightStatus.BOARDING);
        flight.updateStatus(FlightStatus.DEPARTED);

        assertThrows(InvalidStateException.class, pendingOnDepartingFlight::cancelBooking);
        assertEquals(BookingStatus.PENDING, pendingOnDepartingFlight.getStatus());
        assertEquals(SeatStatus.RESERVED, seat1C.getStatus());

        assertThrows(InvalidStateException.class, confirmedOnDepartingFlight::cancelBooking);
        assertEquals(BookingStatus.CONFIRMED, confirmedOnDepartingFlight.getStatus());
        assertEquals(PaymentStatus.COMPLETED, payDep.getStatus());
        assertEquals(SeatStatus.OCCUPIED, seat1D.getStatus());

        flight.updateStatus(FlightStatus.ARRIVED);

        assertThrows(InvalidStateException.class, pendingOnDepartingFlight::cancelBooking);
        assertEquals(BookingStatus.PENDING, pendingOnDepartingFlight.getStatus());
        assertEquals(SeatStatus.RESERVED, seat1C.getStatus());

        assertThrows(InvalidStateException.class, confirmedOnDepartingFlight::cancelBooking);
        assertEquals(BookingStatus.CONFIRMED, confirmedOnDepartingFlight.getStatus());
        assertEquals(PaymentStatus.COMPLETED, payDep.getStatus());
        assertEquals(SeatStatus.OCCUPIED, seat1D.getStatus());
    }
}
