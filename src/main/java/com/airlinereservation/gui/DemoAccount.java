package com.airlinereservation.gui;

/**
 * Quick-fill demo credential descriptor shown on the login screen.
 */
public record DemoAccount(String label, String identifier, String password, String role) {

    @Override
    public String toString() {
        return label;
    }
}
