package com.airlinereservation;

import com.airlinereservation.gui.MainFrame;
import com.airlinereservation.gui.ReservationGuiController;
import com.airlinereservation.system.DemoDataSeeder;

import javax.swing.SwingUtilities;

/**
 * Swing GUI entry point launching {@link MainFrame} on the Event Dispatch Thread.
 */
public class AirlineReservationApp {

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            ReservationGuiController controller = new ReservationGuiController(DemoDataSeeder.createSeededSystem());
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
