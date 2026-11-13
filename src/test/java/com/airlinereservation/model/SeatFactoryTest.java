package com.airlinereservation.model;

import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeatFactoryTest {

    @Test
    void createsBusinessAndEconomySeatsAndRejectsInvalidInputs() {
        BusinessSeat defaultBusiness = SeatFactory.createBusinessSeat(" 1A ");
        assertEquals("1A", defaultBusiness.getSeatNumber());
        assertEquals(SeatStatus.AVAILABLE, defaultBusiness.getStatus());
        assertEquals(2.0, defaultBusiness.getPriceMultiplier(), 0.001);
        assertTrue(defaultBusiness.hasLoungeAccess());

        BusinessSeat customBusiness = SeatFactory.createBusinessSeat("1B", 2.5, false);
        assertEquals("1B", customBusiness.getSeatNumber());
        assertEquals(2.5, customBusiness.getPriceMultiplier(), 0.001);
        assertFalse(customBusiness.hasLoungeAccess());

        EconomySeat standardEconomy = SeatFactory.createEconomySeat(" 2A ", false);
        assertEquals("2A", standardEconomy.getSeatNumber());
        assertFalse(standardEconomy.hasExtraLegroom());

        EconomySeat extraLegroomEconomy = SeatFactory.createEconomySeat("2B", true);
        assertEquals("2B", extraLegroomEconomy.getSeatNumber());
        assertTrue(extraLegroomEconomy.hasExtraLegroom());

        assertThrows(ValidationException.class, () -> SeatFactory.createBusinessSeat(null));
        assertThrows(ValidationException.class, () -> SeatFactory.createBusinessSeat("  "));
        assertThrows(ValidationException.class, () -> SeatFactory.createBusinessSeat("1A", 1.0, true));
        assertThrows(ValidationException.class, () -> SeatFactory.createBusinessSeat("1A", Double.NaN, true));
        assertThrows(ValidationException.class, () -> SeatFactory.createEconomySeat(null, false));
        assertThrows(ValidationException.class, () -> SeatFactory.createEconomySeat(" ", true));
    }

    @Test
    void computesDefaultBusinessSeatCountAndRejectsNonPositiveCapacity() {
        assertEquals(0, SeatFactory.defaultBusinessSeatCount(1));
        assertEquals(0, SeatFactory.defaultBusinessSeatCount(4));
        assertEquals(1, SeatFactory.defaultBusinessSeatCount(5));
        assertEquals(2, SeatFactory.defaultBusinessSeatCount(10));

        assertThrows(ValidationException.class, () -> SeatFactory.defaultBusinessSeatCount(0));
        assertThrows(ValidationException.class, () -> SeatFactory.defaultBusinessSeatCount(-3));
    }

    @Test
    void createsFourColumnSeatMapsWithDefaultAndCustomBusinessCounts() {
        Map<String, Seat> defaultMap = SeatFactory.createSeatMap(10);
        assertEquals(List.of("1A", "1B", "1C", "1D", "2A", "2B", "2C", "2D", "3A", "3B"),
                List.copyOf(defaultMap.keySet()));
        assertInstanceOf(BusinessSeat.class, defaultMap.get("1A"));
        assertInstanceOf(BusinessSeat.class, defaultMap.get("1B"));
        assertTrue(assertInstanceOf(EconomySeat.class, defaultMap.get("1C")).hasExtraLegroom());
        assertTrue(assertInstanceOf(EconomySeat.class, defaultMap.get("1D")).hasExtraLegroom());
        assertFalse(assertInstanceOf(EconomySeat.class, defaultMap.get("2A")).hasExtraLegroom());
        assertFalse(assertInstanceOf(EconomySeat.class, defaultMap.get("3B")).hasExtraLegroom());

        Map<String, Seat> secondMap = SeatFactory.createSeatMap(10);
        assertNotSame(defaultMap.get("1A"), secondMap.get("1A"));

        Map<String, Seat> allEconomy = SeatFactory.createSeatMap(6, 0);
        assertEquals(0, allEconomy.values().stream().filter(BusinessSeat.class::isInstance).count());
        assertTrue(assertInstanceOf(EconomySeat.class, allEconomy.get("1D")).hasExtraLegroom());
        assertFalse(assertInstanceOf(EconomySeat.class, allEconomy.get("2A")).hasExtraLegroom());

        Map<String, Seat> allBusiness = SeatFactory.createSeatMap(6, 6);
        assertEquals(6, allBusiness.values().stream().filter(BusinessSeat.class::isInstance).count());

        assertThrows(UnsupportedOperationException.class,
                () -> defaultMap.put("9Z", SeatFactory.createEconomySeat("9Z", false)));
        assertThrows(ValidationException.class, () -> SeatFactory.createSeatMap(0));
        assertThrows(ValidationException.class, () -> SeatFactory.createSeatMap(0, 0));
        assertThrows(ValidationException.class, () -> SeatFactory.createSeatMap(6, -1));
        assertThrows(ValidationException.class, () -> SeatFactory.createSeatMap(6, 7));
    }
}
