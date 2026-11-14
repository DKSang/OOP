package com.airlinereservation.gui;

import com.airlinereservation.model.Person;

/**
 * Role workspace container for authenticated {@code Admin} sessions.
 */
public class AdminWorkspaceView extends AbstractWorkspaceView {

    public AdminWorkspaceView(ReservationGuiController controller) {
        super(
                controller,
                "Admin Operations Workspace",
                "Flight scheduling, lifecycle cascade actions, and system-wide monitors connect here."
        );
        tabs.getTabs().addAll(
                createPlaceholderTab(
                        "Flight Operations",
                        "Flight Operations",
                        "Admin flight schedule creation and status cascade controls will appear in this tab."
                ),
                createPlaceholderTab(
                        "All Bookings & Passengers",
                        "All Bookings & Passengers",
                        "System-wide booking and passenger monitoring tables will appear in this tab."
                )
        );
    }

    @Override
    protected String formatWelcomeTitle(Person user) {
        return "Operations Console — " + user.getFullName() + " (" + user.getId() + ")";
    }
}
