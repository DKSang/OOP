package com.airlinereservation;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Payment;
import com.airlinereservation.payment.CardPayment;
import com.airlinereservation.payment.EWalletPayment;
import com.airlinereservation.system.AirlineReservationSystem;

import java.time.LocalDate;
import java.util.List;

/**
 * Minimal non-interactive console entry point executing a smoke run of core Milestone 4 workflows.
 */
public class Main {

    public static void main(String[] args) {
        AirlineReservationSystem system = new AirlineReservationSystem();

        Airport han = new Airport("HAN", "Noi Bai", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat", "Ho Chi Minh City");
        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 10);
        system.addAirport(han);
        system.addAirport(sgn);
        system.addAircraft(a320);

        LocalDate travelDate = LocalDate.of(2026, 12, 15);
        Flight mainFlight = new Flight(
                "FL-01", "VN101", han, sgn,
                travelDate.atTime(8, 0), travelDate.atTime(10, 15),
                a320, 1_500_000.0
        );
        Flight backupFlight = new Flight(
                "FL-02", "VN103", han, sgn,
                travelDate.atTime(14, 0), travelDate.atTime(16, 15),
                a320, 1_500_000.0
        );
        system.addFlight(mainFlight);
        system.addFlight(backupFlight);

        Passenger alice = new Passenger(
                "PAX-01", "Nguyen Van A", "a@example.com", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-001"
        );
        Passenger bob = new Passenger(
                "PAX-02", "Tran Thi B", "b@example.com", "0902222222",
                LocalDate.of(1999, 5, 20), "Female", "CCCD-002"
        );
        system.registerPassenger(alice);
        system.registerPassenger(bob);

        // 1. Search
        List<Flight> matches = system.searchFlights("HAN", "SGN", travelDate, 2);
        System.out.println("[1. Search] Found " + matches.size() + " flights from HAN to SGN on " + travelDate);

        // 2. Create Booking
        Booking aliceBooking = system.createBooking(alice.getId(), mainFlight.getFlightId(), List.of("1A", "2A"));
        System.out.println("[2. Create Booking] " + aliceBooking.getBookingId()
                + " status=" + aliceBooking.getStatus()
                + " total=" + aliceBooking.getTotalPrice());

        // 3. Payment retry/confirm
        Payment failedAttempt = system.processPayment(
                aliceBooking.getBookingId(), PaymentMethod.CREDIT_DEBIT_CARD, amount -> false);
        System.out.println("[3a. Payment Retry] " + failedAttempt.getPaymentId()
                + " status=" + failedAttempt.getStatus()
                + " bookingStatus=" + aliceBooking.getStatus());

        Payment confirmedPayment = system.processPayment(
                aliceBooking.getBookingId(),
                PaymentMethod.CREDIT_DEBIT_CARD,
                new CardPayment("4111-XXXX-XXXX-1111", alice.getFullName()));
        System.out.println("[3b. Payment Confirm] " + confirmedPayment.getPaymentId()
                + " status=" + confirmedPayment.getStatus()
                + " bookingStatus=" + aliceBooking.getStatus());

        // 4. Check-in
        system.checkIn(aliceBooking.getBookingId());
        System.out.println("[4. Check-in] " + aliceBooking.getBookingId()
                + " status=" + aliceBooking.getStatus());

        // 5. Cancel/Refund (booking cancel + flight cancel cascade)
        Booking bobBooking = system.createBooking(bob.getId(), mainFlight.getFlightId(), List.of("1B"));
        Payment bobPayment = system.processPayment(
                bobBooking.getBookingId(),
                PaymentMethod.E_WALLET,
                new EWalletPayment("MOMO", bob.getPhoneNumber()));
        system.cancelBooking(bobBooking.getBookingId());
        System.out.println("[5a. Cancel/Refund Booking] " + bobBooking.getBookingId()
                + " status=" + bobBooking.getStatus()
                + " paymentStatus=" + bobPayment.getStatus()
                + " seat1B=" + mainFlight.findSeat("1B").getStatus());

        Booking backupBooking = system.createBooking(bob.getId(), backupFlight.getFlightId(), List.of("2B"));
        Payment backupPayment = system.processPayment(
                backupBooking.getBookingId(),
                PaymentMethod.E_WALLET,
                new EWalletPayment("ZALOPAY", bob.getPhoneNumber()));
        system.cancelFlight(backupFlight.getFlightId());
        System.out.println("[5b. Cancel Flight Cascade] " + backupFlight.getFlightId()
                + " status=" + backupFlight.getStatus()
                + " bookingStatus=" + backupBooking.getStatus()
                + " paymentStatus=" + backupPayment.getStatus());

        // 6. Flight Arrival
        Booking pendingHold = system.createBooking(bob.getId(), mainFlight.getFlightId(), List.of("2C"));
        Booking noShowBooking = system.createBooking(bob.getId(), mainFlight.getFlightId(), List.of("2D"));
        system.processPayment(
                noShowBooking.getBookingId(),
                PaymentMethod.CREDIT_DEBIT_CARD,
                new CardPayment("4111-XXXX-XXXX-2222", bob.getFullName()));

        mainFlight.updateStatus(FlightStatus.BOARDING);
        mainFlight.updateStatus(FlightStatus.DEPARTED);
        system.arriveFlight(mainFlight.getFlightId());
        System.out.println("[6. Flight Arrival] " + mainFlight.getFlightId()
                + " status=" + mainFlight.getStatus()
                + " checkedInBooking=" + aliceBooking.getStatus()
                + " pendingHold=" + pendingHold.getStatus()
                + " noShow=" + noShowBooking.getStatus());
    }
}
