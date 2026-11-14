package com.airlinereservation.system;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Admin;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.payment.CardPayment;

import java.time.LocalDate;
import java.util.List;

/**
 * Deterministic demo catalog and sample booking seeder for GUI startup and demo reset.
 */
public final class DemoDataSeeder {

    public static final LocalDate DEFAULT_TRAVEL_DATE = LocalDate.of(2026, 12, 15);
    public static final String DEMO_ADMIN_PASSWORD = "admin123";

    private DemoDataSeeder() {
    }

    public static AirlineReservationSystem createSeededSystem() {
        AirlineReservationSystem system = new AirlineReservationSystem();
        seed(system);
        return system;
    }

    public static void seed(AirlineReservationSystem system) {
        ValidationException.requireNonNull(system, "Reservation system");

        Airport han = new Airport("HAN", "Noi Bai International Airport", "Hanoi");
        Airport sgn = new Airport("SGN", "Tan Son Nhat International Airport", "Ho Chi Minh City");
        Airport dad = new Airport("DAD", "Da Nang International Airport", "Da Nang");
        system.addAirport(han);
        system.addAirport(sgn);
        system.addAirport(dad);

        Aircraft a320 = new Aircraft("AC-320", "A320neo", "Airbus", 16);
        Aircraft b787 = new Aircraft("AC-787", "787-9 Dreamliner", "Boeing", 20);
        system.addAircraft(a320);
        system.addAircraft(b787);

        Flight vn101 = new Flight(
                "FL-01", "VN101", han, sgn,
                DEFAULT_TRAVEL_DATE.atTime(8, 0),
                DEFAULT_TRAVEL_DATE.atTime(10, 15),
                a320, 1_500_000.0
        );
        Flight vn103 = new Flight(
                "FL-02", "VN103", han, sgn,
                DEFAULT_TRAVEL_DATE.atTime(14, 0),
                DEFAULT_TRAVEL_DATE.atTime(16, 15),
                b787, 1_850_000.0
        );
        Flight vn205 = new Flight(
                "FL-03", "VN205", sgn, dad,
                DEFAULT_TRAVEL_DATE.plusDays(1).atTime(9, 30),
                DEFAULT_TRAVEL_DATE.plusDays(1).atTime(10, 50),
                a320, 1_200_000.0
        );
        system.addFlight(vn101);
        system.addFlight(vn103);
        system.addFlight(vn205);

        Passenger pax1 = new Passenger(
                "PAX-01", "Nguyen Van An", "an.nguyen@airline.vn", "0901111111",
                LocalDate.of(1998, 1, 10), "Male", "CCCD-0001"
        );
        Passenger pax2 = new Passenger(
                "PAX-02", "Tran Thi Binh", "binh.tran@airline.vn", "0902222222",
                LocalDate.of(1999, 5, 20), "Female", "CCCD-0002"
        );
        system.registerPassenger(pax1);
        system.registerPassenger(pax2);

        Admin admin = new Admin(
                "ADM-01", "Le Minh Cuong", "admin@airline.vn", "0909999999",
                "STF-001", "Flight Operations", DEMO_ADMIN_PASSWORD
        );
        system.registerAdmin(admin);

        Booking confirmedSample = system.createBooking(pax1.getId(), vn101.getFlightId(), List.of("1A"));
        system.processPayment(
                confirmedSample.getBookingId(),
                PaymentMethod.CREDIT_DEBIT_CARD,
                new CardPayment("4111-XXXX-XXXX-1111", pax1.getFullName())
        );

        system.createBooking(pax1.getId(), vn103.getFlightId(), List.of("2A", "2B"));
    }

    public static void reset(AirlineReservationSystem system) {
        ValidationException.requireNonNull(system, "Reservation system");
        system.clear();
        seed(system);
    }
}
