package com.airlinereservation.system;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.EntityNotFoundException;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Payment;
import com.airlinereservation.model.Seat;
import com.airlinereservation.payment.BankTransferPayment;
import com.airlinereservation.payment.CardPayment;
import com.airlinereservation.payment.CashPayment;
import com.airlinereservation.payment.EWalletPayment;
import com.airlinereservation.payment.PaymentFactory;
import com.airlinereservation.payment.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirlineReservationSystemPaymentTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-11-13T16:30:00Z");

    private AirlineReservationSystem system;
    private Airport han;
    private Airport sgn;
    private Aircraft a320;
    private Flight flight;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        system = new AirlineReservationSystem(Clock.fixed(FIXED_INSTANT, ZONE));
        han = new Airport("HAN", "Noi Bai", "Hanoi");
        sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);

        system.addAirport(han);
        system.addAirport(sgn);
        system.addAircraft(a320);

        flight = new Flight(
                "FL-01", "VN101", han, sgn,
                LocalDateTime.of(2026, 12, 10, 7, 0),
                LocalDateTime.of(2026, 12, 10, 9, 15),
                a320, 1_000_000, 4
        );
        system.addFlight(flight);

        passenger = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        system.registerPassenger(passenger);
    }

    @Test
    void processesAllFourRealPaymentStrategiesAndConfirmsBookingsWithOccupiedSeats() {
        Booking cardBooking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        Payment cardPayment = system.processPayment(
                cardBooking.getBookingId(),
                PaymentMethod.CREDIT_DEBIT_CARD,
                new CardPayment("4111-2222-3333-4444", "Nguyen Van A")
        );
        assertEquals(PaymentStatus.COMPLETED, cardPayment.getStatus());
        assertEquals(2_000_000.0, cardPayment.getAmount(), 0.001);
        assertEquals(LocalDateTime.ofInstant(FIXED_INSTANT, ZONE), cardPayment.getPaidAt());
        assertEquals(BookingStatus.CONFIRMED, cardBooking.getStatus());
        assertSame(cardPayment, cardBooking.getPayment());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("1A").getStatus());

        Booking walletBooking = system.createBooking("PAX-01", "FL-01", List.of("2A"));
        Payment walletPayment = system.processPayment(
                walletBooking,
                PaymentMethod.E_WALLET,
                new EWalletPayment("0901111111", "MoMo")
        );
        assertEquals(PaymentStatus.COMPLETED, walletPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, walletBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("2A").getStatus());

        Booking transferBooking = system.createBooking("PAX-01", "FL-01", List.of("3A"));
        Payment transferPayment = system.processPayment(
                transferBooking.getBookingId(),
                PaymentMethod.BANK_TRANSFER,
                new BankTransferPayment("Vietcombank", "0123456789")
        );
        assertEquals(PaymentStatus.COMPLETED, transferPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, transferBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("3A").getStatus());

        Booking cashBooking = system.createBooking(passenger, flight, List.of(flight.findSeat("3B")));
        Payment cashPayment = system.processPayment(
                cashBooking,
                PaymentMethod.CASH,
                new CashPayment("CNT-01", "ST-02")
        );
        assertEquals(PaymentStatus.COMPLETED, cashPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, cashBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("3B").getStatus());
    }

    @Test
    void paysPendingBookingWhileFlightIsBoardingUsingCashStrategy() {
        Booking cashBooking = system.createBooking("PAX-01", "FL-01", List.of("3B"));
        flight.updateStatus(FlightStatus.BOARDING);

        Payment cashPayment = system.processPayment(
                cashBooking.getBookingId(),
                PaymentMethod.CASH,
                new CashPayment("CNT-01", "ST-02")
        );

        assertEquals(PaymentStatus.COMPLETED, cashPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, cashBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("3B").getStatus());
    }

    @Test
    void keepsBookingPendingAndSeatsReservedOnFailedPaymentThenAllowsRetryToConfirm() {
        Booking booking = system.createBooking("PAX-01", "FL-01", List.of("1A", "2A"));
        Seat seat1A = flight.findSeat("1A");
        Seat seat2A = flight.findSeat("2A");

        Payment failedAttempt = system.processPayment(booking.getBookingId(), PaymentMethod.E_WALLET, amount -> false);

        assertEquals(PaymentStatus.FAILED, failedAttempt.getStatus());
        assertNull(failedAttempt.getPaidAt());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNull(booking.getPayment());
        assertEquals(SeatStatus.RESERVED, seat1A.getStatus());
        assertEquals(SeatStatus.RESERVED, seat2A.getStatus());

        Payment retrySuccess = system.processPayment(
                booking.getBookingId(),
                PaymentMethod.CREDIT_DEBIT_CARD,
                amount -> true
        );

        assertNotEquals(failedAttempt.getPaymentId(), retrySuccess.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, retrySuccess.getStatus());
        assertEquals(LocalDateTime.ofInstant(FIXED_INSTANT, ZONE), retrySuccess.getPaidAt());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertSame(retrySuccess, booking.getPayment());
        assertEquals(SeatStatus.OCCUPIED, seat1A.getStatus());
        assertEquals(SeatStatus.OCCUPIED, seat2A.getStatus());
    }

    @Test
    void rejectsInvalidPaymentRequestsBeforeInvokingStrategy() {
        Booking booking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        AtomicBoolean strategyInvoked = new AtomicBoolean(false);

        assertThrows(ValidationException.class,
                () -> system.processPayment((String) null, PaymentMethod.CASH, amount -> true));
        assertThrows(ValidationException.class,
                () -> system.processPayment("  ", PaymentMethod.CASH, amount -> true));
        assertThrows(EntityNotFoundException.class,
                () -> system.processPayment("BK-404", PaymentMethod.CASH, amount -> true));
        assertThrows(ValidationException.class,
                () -> system.processPayment(booking.getBookingId(), null, amount -> true));
        assertThrows(ValidationException.class,
                () -> system.processPayment(booking.getBookingId(), PaymentMethod.CASH, null));

        Booking unregistered = new Booking(
                booking.getBookingId(), flight, passenger,
                List.of(flight.findSeat("1B")), LocalDateTime.of(2026, 11, 13, 12, 0)
        );
        assertThrows(ValidationException.class,
                () -> system.processPayment(unregistered, PaymentMethod.CASH, amount -> {
                    strategyInvoked.set(true);
                    return true;
                }));
        assertFalse(strategyInvoked.get());

        // Cancel flight -> payment rejected without invoking strategy
        flight.cancel();
        assertThrows(InvalidStateException.class,
                () -> system.processPayment(booking.getBookingId(), PaymentMethod.CASH, amount -> {
                    strategyInvoked.set(true);
                    return true;
                }));
        assertFalse(strategyInvoked.get());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(SeatStatus.RESERVED, flight.findSeat("1A").getStatus());

        // Already confirmed booking -> rejected without invoking strategy
        Flight secondFlight = new Flight(
                "FL-02", "VN102", han, sgn,
                LocalDateTime.of(2026, 12, 11, 7, 0),
                LocalDateTime.of(2026, 12, 11, 9, 15),
                a320, 1_000_000, 4
        );
        system.addFlight(secondFlight);
        Booking confirmed = system.createBooking("PAX-01", "FL-02", List.of("1A"));
        system.processPayment(confirmed.getBookingId(), PaymentMethod.CASH, amount -> true);

        assertThrows(InvalidStateException.class,
                () -> system.processPayment(confirmed.getBookingId(), PaymentMethod.CASH, amount -> {
                    strategyInvoked.set(true);
                    return true;
                }));
        assertFalse(strategyInvoked.get());

        // Departed / Arrived flight -> rejected without invoking strategy
        Booking pendingOnSecondFlight = system.createBooking("PAX-01", "FL-02", List.of("1B"));
        secondFlight.updateStatus(FlightStatus.BOARDING);
        secondFlight.updateStatus(FlightStatus.DEPARTED);
        assertThrows(InvalidStateException.class,
                () -> system.processPayment(pendingOnSecondFlight.getBookingId(), PaymentMethod.CASH, amount -> {
                    strategyInvoked.set(true);
                    return true;
                }));
        assertFalse(strategyInvoked.get());
        assertEquals(BookingStatus.PENDING, pendingOnSecondFlight.getStatus());
        assertEquals(SeatStatus.RESERVED, secondFlight.findSeat("1B").getStatus());
    }

    @Test
    void rejectsMismatchedMethodAndConcreteStrategyWithoutMutatingBookingSeatsOrPaymentSequence() {
        Booking booking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        Seat seat1A = flight.findSeat("1A");

        assertThrows(ValidationException.class, () -> system.processPayment(
                booking.getBookingId(),
                PaymentMethod.CASH,
                PaymentFactory.createCardPayment("4111-2222-3333-4444", "Nguyen Van A")
        ));
        assertThrows(ValidationException.class, () -> system.processPayment(
                booking,
                PaymentMethod.E_WALLET,
                PaymentFactory.createCashPayment("CNT-01", "ST-01")
        ));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNull(booking.getPayment());
        assertEquals(SeatStatus.RESERVED, seat1A.getStatus());

        Payment validPayment = system.processPayment(
                booking.getBookingId(),
                PaymentMethod.CASH,
                PaymentFactory.createCashPayment("CNT-01", "ST-01")
        );
        assertEquals("PAY-1", validPayment.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, validPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, seat1A.getStatus());
    }

    @Test
    void infersPaymentMethodForStrategyOnlyOverloadsAndRejectsGenericLambdas() {
        Booking cardBooking = system.createBooking("PAX-01", "FL-01", List.of("1A"));
        Payment cardPayment = system.processPayment(
                cardBooking.getBookingId(),
                PaymentFactory.createCardPayment("4111-2222-3333-4444", "Nguyen Van A")
        );
        assertEquals(PaymentMethod.CREDIT_DEBIT_CARD, cardPayment.getMethod());
        assertEquals(PaymentStatus.COMPLETED, cardPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, cardBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("1A").getStatus());

        Booking walletBooking = system.createBooking("PAX-01", "FL-01", List.of("2A"));
        Payment walletPayment = system.processPayment(
                walletBooking,
                PaymentFactory.createEWalletPayment("0901111111", "MoMo")
        );
        assertEquals(PaymentMethod.E_WALLET, walletPayment.getMethod());
        assertEquals(PaymentStatus.COMPLETED, walletPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, walletBooking.getStatus());
        assertEquals(SeatStatus.OCCUPIED, flight.findSeat("2A").getStatus());

        Booking pending = system.createBooking("PAX-01", "FL-01", List.of("3A"));
        assertThrows(ValidationException.class,
                () -> system.processPayment(pending.getBookingId(), (PaymentStrategy) amount -> true));
        assertThrows(ValidationException.class,
                () -> system.processPayment(pending, (PaymentStrategy) amount -> true));
        assertThrows(ValidationException.class,
                () -> system.processPayment(pending.getBookingId(), (PaymentStrategy) null));
        assertThrows(ValidationException.class,
                () -> system.processPayment(pending, (PaymentStrategy) null));
        assertThrows(EntityNotFoundException.class,
                () -> system.processPayment("BK-404", PaymentFactory.createCashPayment("CNT-01", "ST-01")));

        assertEquals(BookingStatus.PENDING, pending.getStatus());
        assertNull(pending.getPayment());
        assertEquals(SeatStatus.RESERVED, flight.findSeat("3A").getStatus());
    }
}
