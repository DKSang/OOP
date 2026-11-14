package com.airlinereservation.gui;

import com.airlinereservation.model.Person;

/**
 * Role workspace container for authenticated {@code Passenger} sessions.
 */
public class PassengerWorkspacePanel extends AbstractWorkspacePanel {

    public PassengerWorkspacePanel(ReservationGuiController controller) {
        super(
                controller,
                "Passenger Workspace",
                "Flight search, interactive seat selection, and booking payment tabs connect here."
        );
        tabs.addTab("Search & Book Flights", createPlaceholderTab(
                "Search & Book Flights",
                "Passenger flight search and seat map selection will appear in this workspace tab."
        ));
        tabs.addTab("My Bookings", createPlaceholderTab(
                "My Bookings",
                "Passenger booking history, payment retry, check-in, and cancellation will appear here."
        ));
    }

    @Override
    protected String formatWelcomeTitle(Person user) {
        return "Welcome, " + user.getFullName() + " (" + user.getId() + ")";
    }
}
