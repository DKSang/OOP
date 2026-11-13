package com.airlinereservation.model;

import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.InvalidStateException;
import com.airlinereservation.exception.SeatNotAvailableException;
import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeatTest {

    @Test
    void calculatesPolymorphicPricesForEconomyAndBusinessSeats() {
        Seat standardEconomy = new EconomySeat(" 2A ", false);
        Seat extraLegroomEconomy = new EconomySeat("2B", true);
        Seat defaultBusiness = new BusinessSeat("1A");
        Seat customBusiness = new BusinessSeat("1B", 2.5, false);

        assertEquals("2A", standardEconomy.getSeatNumber());
        assertEquals(SeatStatus.AVAILABLE, standardEconomy.getStatus());
        assertEquals("Economy", standardEconomy.getSeatType());
        assertFalse(((EconomySeat) standardEconomy).hasExtraLegroom());
        assertEquals(1_000_000.0, standardEconomy.calculatePrice(1_000_000.0), 0.001);

        assertTrue(((EconomySeat) extraLegroomEconomy).hasExtraLegroom());
        assertEquals(1_150_000.0, extraLegroomEconomy.calculatePrice(1_000_000.0), 0.001);

        assertEquals("Business", defaultBusiness.getSeatType());
        assertEquals(2.0, ((BusinessSeat) defaultBusiness).getPriceMultiplier(), 0.001);
        assertTrue(((BusinessSeat) defaultBusiness).hasLoungeAccess());
        assertEquals(2_000_000.0, defaultBusiness.calculatePrice(1_000_000.0), 0.001);

        assertFalse(((BusinessSeat) customBusiness).hasLoungeAccess());
        assertEquals(2_500_000.0, customBusiness.calculatePrice(1_000_000.0), 0.001);
    }

    @Test
    void rejectsInvalidSeatArgumentsAndBasePrices() {
        assertThrows(ValidationException.class, () -> new EconomySeat(null, false));
        assertThrows(ValidationException.class, () -> new EconomySeat("  ", true));
        assertThrows(ValidationException.class, () -> new BusinessSeat("1A", 1.0, true));
        assertThrows(ValidationException.class, () -> new BusinessSeat("1A", 0.9, true));

        Seat economy = new EconomySeat("2A", false);
        Seat business = new BusinessSeat("1A");
        assertThrows(ValidationException.class, () -> economy.calculatePrice(0));
        assertThrows(ValidationException.class, () -> economy.calculatePrice(-500));
        assertThrows(ValidationException.class, () -> business.calculatePrice(0));
    }

    @Test
    void transitionsSeatStatusAndRejectsInvalidTransitions() {
        Seat seat = new EconomySeat("3A", false);
        assertTrue(seat.isAvailable());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());

        assertThrows(InvalidStateException.class, seat::occupy);
        assertThrows(InvalidStateException.class, seat::release);

        seat.reserve();
        assertFalse(seat.isAvailable());
        assertEquals(SeatStatus.RESERVED, seat.getStatus());
        assertThrows(SeatNotAvailableException.class, seat::reserve);

        seat.occupy();
        assertEquals(SeatStatus.OCCUPIED, seat.getStatus());
        assertThrows(SeatNotAvailableException.class, seat::reserve);
        assertThrows(InvalidStateException.class, seat::occupy);

        seat.release();
        assertTrue(seat.isAvailable());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());

        seat.reserve();
        seat.release();
        assertTrue(seat.isAvailable());
    }
}
