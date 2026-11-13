package com.airlinereservation.model;

import com.airlinereservation.exception.ValidationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Factory for creating individual cabin seats and per-flight seat maps.
 */
public final class SeatFactory {

    private static final int COLUMNS_PER_ROW = 4;
    private static final int SMALL_AIRCRAFT_CAPACITY_THRESHOLD = 5;
    private static final double DEFAULT_BUSINESS_RATIO = 0.2;

    private SeatFactory() {
    }

    public static BusinessSeat createBusinessSeat(String seatNumber) {
        return new BusinessSeat(seatNumber);
    }

    public static BusinessSeat createBusinessSeat(String seatNumber, double priceMultiplier, boolean hasLoungeAccess) {
        return new BusinessSeat(seatNumber, priceMultiplier, hasLoungeAccess);
    }

    public static EconomySeat createEconomySeat(String seatNumber, boolean hasExtraLegroom) {
        return new EconomySeat(seatNumber, hasExtraLegroom);
    }

    public static int defaultBusinessSeatCount(int capacity) {
        requirePositiveCapacity(capacity);
        return capacity >= SMALL_AIRCRAFT_CAPACITY_THRESHOLD
                ? (int) Math.ceil(capacity * DEFAULT_BUSINESS_RATIO)
                : 0;
    }

    public static Map<String, Seat> createSeatMap(int capacity) {
        return createSeatMap(capacity, defaultBusinessSeatCount(capacity));
    }

    public static Map<String, Seat> createSeatMap(int capacity, int businessSeatCount) {
        requirePositiveCapacity(capacity);
        if (businessSeatCount < 0 || businessSeatCount > capacity) {
            throw new ValidationException("Business seat count must be between 0 and " + capacity);
        }
        Map<String, Seat> seatMap = new LinkedHashMap<>();
        int firstEconomyRow = (businessSeatCount / COLUMNS_PER_ROW) + 1;
        for (int index = 0; index < capacity; index++) {
            int row = (index / COLUMNS_PER_ROW) + 1;
            char column = (char) ('A' + (index % COLUMNS_PER_ROW));
            String seatNumber = row + String.valueOf(column);
            Seat seat = index < businessSeatCount
                    ? createBusinessSeat(seatNumber)
                    : createEconomySeat(seatNumber, row == firstEconomyRow);
            seatMap.put(seatNumber, seat);
        }
        return Collections.unmodifiableMap(seatMap);
    }

    private static void requirePositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new ValidationException("Seat capacity must be positive");
        }
    }
}
