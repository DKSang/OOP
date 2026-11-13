package com.airlinereservation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void runsNonInteractiveSmokeScenarioToConsole() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String output = captured.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Search"));
        assertTrue(output.contains("Create Booking"));
        assertTrue(output.contains("FAILED"));
        assertTrue(output.contains("CONFIRMED"));
        assertTrue(output.contains("CHECKED_IN"));
        assertTrue(output.contains("REFUNDED"));
        assertTrue(output.contains("ARRIVED"));
        assertTrue(output.contains("COMPLETED"));
    }
}
