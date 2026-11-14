package com.airlinereservation.system;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.ValidationException;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Passenger;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataSeederTest {

    @Test
    void seedsDeterministicCatalogUsersAndSampleBookings() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();

        assertEquals(3, system.getAirports().size());
        assertNotNull(system.findAirportByCode("HAN"));
        assertNotNull(system.findAirportByCode("SGN"));
        assertNotNull(system.findAirportByCode("DAD"));

        assertEquals(2, system.getAircrafts().size());
        assertNotNull(system.findAircraftById("AC-320"));
        assertNotNull(system.findAircraftById("AC-787"));

        assertEquals(3, system.getFlights().size());
        assertEquals("VN101", system.findFlightById("FL-01").getFlightNumber());
        assertEquals("VN103", system.findFlightById("FL-02").getFlightNumber());
        assertEquals("VN205", system.findFlightById("FL-03").getFlightNumber());

        assertEquals(2, system.getPassengers().size());
        assertNotNull(system.findPassengerById("PAX-01"));
        assertNotNull(system.findPassengerById("PAX-02"));

        assertEquals(1, system.getAdmins().size());
        assertNotNull(system.findAdminById("ADM-01"));

        List<Booking> bookings = system.getBookings();
        assertEquals(2, bookings.size());
        assertEquals(BookingStatus.CONFIRMED, bookings.get(0).getStatus());
        assertEquals(PaymentStatus.COMPLETED, bookings.get(0).getPayment().getStatus());
        assertEquals(BookingStatus.PENDING, bookings.get(1).getStatus());
        assertEquals(SeatStatus.OCCUPIED, system.findFlightById("FL-01").findSeat("1A").getStatus());
        assertEquals(SeatStatus.RESERVED, system.findFlightById("FL-02").findSeat("2A").getStatus());
    }

    @Test
    void resetsMutatedSystemBackToCleanInitialDemoState() {
        AirlineReservationSystem system = DemoDataSeeder.createSeededSystem();

        Passenger extraPassenger = new Passenger(
                "PAX-99", "Extra User", "extra@example.com", "0909998888",
                LocalDate.of(1995, 3, 12), "Female", "CCCD-999"
        );
        system.registerPassenger(extraPassenger);
        system.createBooking("PAX-99", "FL-03", List.of("1A", "1B"));
        system.cancelFlight("FL-01");

        DemoDataSeeder.reset(system);

        assertEquals(2, system.getPassengers().size());
        assertEquals(2, system.getBookings().size());
        assertEquals(SeatStatus.AVAILABLE, system.findFlightById("FL-03").findSeat("1A").getStatus());
        assertTrue(system.findFlightById("FL-01").canAcceptNewBooking());
        assertEquals("BK-1", system.getBookings().get(0).getBookingId());

        assertThrows(ValidationException.class, () -> DemoDataSeeder.seed(null));
        assertThrows(ValidationException.class, () -> DemoDataSeeder.reset(null));
    }
}
