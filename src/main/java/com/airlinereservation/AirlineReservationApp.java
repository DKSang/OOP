package com.airlinereservation;

import com.airlinereservation.gui.AirlineReservationFxApp;
import javafx.application.Application;

/**
 * Desktop GUI launcher delegating to {@link AirlineReservationFxApp} so classpath launches
 * (including {@code Main --gui}) work seamlessly alongside {@code mvn javafx:run}.
 */
public class AirlineReservationApp {

    public static void main(String[] args) {
        launch(args);
    }

    public static void launch() {
        launch(new String[0]);
    }

    public static void launch(String[] args) {
        Application.launch(AirlineReservationFxApp.class, args == null ? new String[0] : args);
    }
}
