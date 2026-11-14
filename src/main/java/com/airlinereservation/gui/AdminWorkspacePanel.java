package com.airlinereservation.gui;

import com.airlinereservation.model.Person;

/**
 * Role workspace container for authenticated {@code Admin} sessions.
 */
public class AdminWorkspacePanel extends AbstractWorkspacePanel {

    public AdminWorkspacePanel(ReservationGuiController controller) {
        super(
                controller,
                "Admin Operations Workspace",
                "Flight scheduling, lifecycle cascade actions, and system-wide monitors connect here."
        );
        tabs.addTab("Flight Operations", createPlaceholderTab(
                "Flight Operations",
                "Admin flight schedule creation and status cascade controls will appear in this tab."
        ));
        tabs.addTab("All Bookings & Passengers", createPlaceholderTab(
                "All Bookings & Passengers",
                "System-wide booking and passenger monitoring tables will appear in this tab."
        ));
    }

    @Override
    protected String formatWelcomeTitle(Person user) {
        return "Operations Console — " + user.getFullName() + " (" + user.getId() + ")";
    }
}
