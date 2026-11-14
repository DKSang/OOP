package com.airlinereservation.gui;

import com.airlinereservation.system.DemoDataSeeder;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX {@link Application} entry point initializing the seeded reservation system
 * and primary {@link MainWindow}.
 */
public class AirlineReservationFxApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ReservationGuiController controller = new ReservationGuiController(DemoDataSeeder.createSeededSystem());
        MainWindow mainWindow = new MainWindow(primaryStage, controller);
        mainWindow.show();
    }
}
