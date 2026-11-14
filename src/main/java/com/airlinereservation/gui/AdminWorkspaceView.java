package com.airlinereservation.gui;

import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Aircraft;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;

/**
 * Role workspace container for authenticated {@code Admin} sessions.
 */
public class AdminWorkspaceView extends AbstractWorkspaceView {

    private final TextField flightIdField = new TextField();
    private final TextField flightNumberField = new TextField();
    private final ComboBox<Airport> departureCombo = new ComboBox<>();
    private final ComboBox<Airport> arrivalCombo = new ComboBox<>();
    private final ComboBox<Aircraft> aircraftCombo = new ComboBox<>();
    private final TextField departureTimeField = new TextField();
    private final TextField arrivalTimeField = new TextField();
    private final TextField basePriceField = new TextField();

    private final TableView<Flight> flightTable = new TableView<>();
    private final Label flightCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 flight(s)");
    private final Label flightLifecycleHintLabel = ArbitrumTheme.createMutedLabel(
            "Select a flight row to transition status or trigger arrival/cancellation cascades."
    );

    private final Button boardingButton = ArbitrumTheme.createSecondaryButton("Mark Boarding");
    private final Button departedButton = ArbitrumTheme.createSecondaryButton("Mark Departed");
    private final Button arrivedButton = ArbitrumTheme.createPrimaryButton("Mark Arrived");
    private final Button cancelFlightButton = new Button("Cancel Flight");

    private final Label totalFlightsMetricLabel = createAdminStatValueLabel();
    private final Label totalBookingsMetricLabel = createAdminStatValueLabel();
    private final Label registeredPassengersMetricLabel = createAdminStatValueLabel();
    private final Label settledRevenueMetricLabel = createAdminStatValueLabel();

    private final TableView<Booking> bookingTable = new TableView<>();
    private final Label adminBookingCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 booking(s)");

    private final TableView<Passenger> passengerTable = new TableView<>();
    private final Label passengerCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 passenger(s)");

    private final Tab flightOperationsTab;
    private final Tab bookingsLedgerTab;
    private final Tab passengerRegistryTab;

    public AdminWorkspaceView(ReservationGuiController controller) {
        super(
                controller,
                "ADMIN OPERATIONS CONSOLE",
                "Admin Operations Workspace",
                "Dispatch flights, drive lifecycle state transitions, and audit system-wide bookings and passengers."
        );
        this.flightOperationsTab = createNonClosableTab("Flight Operations", createFlightOperationsTab());
        this.bookingsLedgerTab = createNonClosableTab("Booking Ledger", createBookingsLedgerTab());
        this.passengerRegistryTab = createNonClosableTab("Passenger Registry", createPassengerRegistryTab());
        tabs.getTabs().addAll(flightOperationsTab, bookingsLedgerTab, passengerRegistryTab);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            refreshAllTablesAndMetrics();
        });
    }

    @Override
    public void refreshData() {
        super.refreshData();
        populateCatalogSelectors();
        resetFlightFormDefaults();
        refreshAllTablesAndMetrics();
    }

    @Override
    protected String formatWelcomeTitle(Person user) {
        return user.getFullName() + " · " + user.getId();
    }

    private BorderPane createFlightOperationsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(4, 0, 0, 0));

        VBox formCard = createScheduleFlightCard();
        BorderPane.setMargin(formCard, new Insets(0, 0, ArbitrumTheme.SPACE_12, 0));

        configureFlightTable();
        BorderPane actionBar = createFlightActionBar();
        BorderPane.setMargin(actionBar, new Insets(ArbitrumTheme.SPACE_12, 0, 0, 0));

        layout.setTop(formCard);
        layout.setCenter(flightTable);
        layout.setBottom(actionBar);
        return layout;
    }

    private VBox createScheduleFlightCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label sectionTitle = ArbitrumTheme.createHeadingLabel("Schedule New Flight", "heading-md");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button autoFillButton = ArbitrumTheme.createSecondaryButton("Suggest Next ID");
        autoFillButton.setOnAction(e -> suggestNextFlightIdentity());

        Button scheduleButton = ArbitrumTheme.createPrimaryButton("Schedule Flight");
        scheduleButton.setOnAction(e -> handleScheduleFlight());

        HBox headerRow = new HBox(ArbitrumTheme.SPACE_12, sectionTitle, spacer, autoFillButton, scheduleButton);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        GridPane fieldsGrid = new GridPane();
        fieldsGrid.setHgap(ArbitrumTheme.SPACE_12);
        fieldsGrid.setVgap(8);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            fieldsGrid.getColumnConstraints().add(col);
        }

        ArbitrumTheme.styleTextInput(flightIdField);
        flightIdField.setPromptText("e.g. FL-04");

        ArbitrumTheme.styleTextInput(flightNumberField);
        flightNumberField.setPromptText("e.g. VN301");

        configureAirportCombo(departureCombo);
        configureAirportCombo(arrivalCombo);
        configureAircraftCombo(aircraftCombo);

        ArbitrumTheme.styleTextInput(departureTimeField);
        departureTimeField.setPromptText("YYYY-MM-DD HH:MM");

        ArbitrumTheme.styleTextInput(arrivalTimeField);
        arrivalTimeField.setPromptText("YYYY-MM-DD HH:MM");

        ArbitrumTheme.styleTextInput(basePriceField);
        basePriceField.setPromptText("e.g. 1500000");
        basePriceField.setOnAction(e -> handleScheduleFlight());

        fieldsGrid.add(labeledField("Flight ID", flightIdField), 0, 0);
        fieldsGrid.add(labeledField("Flight Number", flightNumberField), 1, 0);
        fieldsGrid.add(labeledField("From Airport", departureCombo), 2, 0);
        fieldsGrid.add(labeledField("To Airport", arrivalCombo), 3, 0);

        fieldsGrid.add(labeledField("Operating Aircraft", aircraftCombo), 0, 1);
        fieldsGrid.add(labeledField("Departure (YYYY-MM-DD HH:MM)", departureTimeField), 1, 1);
        fieldsGrid.add(labeledField("Arrival (YYYY-MM-DD HH:MM)", arrivalTimeField), 2, 1);
        fieldsGrid.add(labeledField("Base Price (VND)", basePriceField), 3, 1);

        card.getChildren().addAll(headerRow, fieldsGrid);
        return card;
    }

    private void configureFlightTable() {
        configureExplorerTable(flightTable, "No flights scheduled in the system.");
        flightTable.getColumns().setAll(buildFlightExplorerColumns());

        flightTable.getSelectionModel().selectedItemProperty().addListener((obs, oldFlight, newFlight) -> {
            updateFlightLifecycleButtons(newFlight);
        });
        updateFlightLifecycleButtons(null);
    }

    private BorderPane createFlightActionBar() {
        BorderPane bar = new BorderPane();
        bar.getStyleClass().add("action-footer-bar");

        VBox leftMeta = new VBox(2, flightCountLabel, flightLifecycleHintLabel);
        leftMeta.setAlignment(Pos.CENTER_LEFT);

        boardingButton.setOnAction(e -> handleStatusTransition(FlightStatus.BOARDING));
        departedButton.setOnAction(e -> handleStatusTransition(FlightStatus.DEPARTED));
        arrivedButton.setOnAction(e -> handleArriveFlight());

        cancelFlightButton.getStyleClass().add("btn-danger-pill");
        cancelFlightButton.setOnAction(e -> handleCancelFlight());

        HBox actions = new HBox(
                ArbitrumTheme.SPACE_12,
                boardingButton,
                departedButton,
                arrivedButton,
                cancelFlightButton
        );
        actions.setAlignment(Pos.CENTER_RIGHT);

        bar.setLeft(leftMeta);
        bar.setRight(actions);
        return bar;
    }

    private void updateFlightLifecycleButtons(Flight selected) {
        if (selected == null) {
            boardingButton.setDisable(true);
            departedButton.setDisable(true);
            arrivedButton.setDisable(true);
            cancelFlightButton.setDisable(true);
            flightLifecycleHintLabel.setText(
                    "Select a flight row to transition status or trigger arrival/cancellation cascades."
            );
            return;
        }

        FlightStatus status = selected.getStatus();
        boardingButton.setDisable(status != FlightStatus.SCHEDULED);
        departedButton.setDisable(status != FlightStatus.BOARDING);
        arrivedButton.setDisable(status != FlightStatus.DEPARTED);
        cancelFlightButton.setDisable(status != FlightStatus.SCHEDULED && status != FlightStatus.BOARDING);

        String lifecycleGuidance = switch (status) {
            case SCHEDULED -> "Next step: Mark Boarding (or Cancel Flight with automatic refund cascade).";
            case BOARDING -> "Gate open — Next step: Mark Departed (or Cancel Flight).";
            case DEPARTED -> "In flight — Next step: Mark Arrived (completes checked-in bookings & releases unpaid holds).";
            case ARRIVED -> "Flight arrived — all booking lifecycle cascades completed.";
            case CANCELLED -> "Flight cancelled — all seats released and paid bookings refunded.";
        };
        flightLifecycleHintLabel.setText(
                selected.getFlightNumber() + " (" + selected.getFlightId() + " · " + status + "): " + lifecycleGuidance
        );
    }

    private BorderPane createBookingsLedgerTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(4, 0, 0, 0));

        HBox telemetryBar = createTelemetryRow();
        BorderPane.setMargin(telemetryBar, new Insets(0, 0, ArbitrumTheme.SPACE_12, 0));

        configureAdminBookingTable();

        BorderPane footerBar = new BorderPane();
        footerBar.getStyleClass().add("action-footer-bar");
        footerBar.setLeft(adminBookingCountLabel);
        footerBar.setRight(ArbitrumTheme.createMutedLabel(
                "System-wide ledger reflecting real-time passenger reservations, settlements, check-ins, and refunds."
        ));
        BorderPane.setMargin(footerBar, new Insets(ArbitrumTheme.SPACE_12, 0, 0, 0));

        layout.setTop(telemetryBar);
        layout.setCenter(bookingTable);
        layout.setBottom(footerBar);
        return layout;
    }

    private BorderPane createPassengerRegistryTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(4, 0, 0, 0));

        configurePassengerTable();

        BorderPane footerBar = new BorderPane();
        footerBar.getStyleClass().add("action-footer-bar");
        footerBar.setLeft(passengerCountLabel);
        footerBar.setRight(ArbitrumTheme.createMutedLabel(
                "Verified passenger identities and cumulative booking counts."
        ));
        BorderPane.setMargin(footerBar, new Insets(ArbitrumTheme.SPACE_12, 0, 0, 0));

        layout.setCenter(passengerTable);
        layout.setBottom(footerBar);
        return layout;
    }

    private HBox createTelemetryRow() {
        HBox tilesRow = new HBox(
                ArbitrumTheme.SPACE_12,
                buildAdminStatTile("TOTAL FLIGHTS", totalFlightsMetricLabel),
                buildAdminStatTile("TOTAL BOOKINGS", totalBookingsMetricLabel),
                buildAdminStatTile("REGISTERED PASSENGERS", registeredPassengersMetricLabel),
                buildAdminStatTile("SETTLED REVENUE", settledRevenueMetricLabel)
        );
        return tilesRow;
    }

    private void configureAdminBookingTable() {
        configureExplorerTable(bookingTable, "No bookings recorded in the system.");

        String[] cols = ReservationGuiController.ADMIN_BOOKING_TABLE_COLUMNS;
        bookingTable.getColumns().setAll(List.of(
                textColumn(cols[0], Booking::getBookingId, 90, true),
                textColumn(cols[1], b -> ReservationGuiController.formatPassengerSummary(b.getPassenger()), 190, false),
                textColumn(cols[2], b -> b.getFlight().getFlightNumber(), 90, true),
                textColumn(cols[3], b -> ReservationGuiController.formatRoute(b.getFlight()), 115, true),
                textColumn(cols[4], ReservationGuiController::formatSeatNumbers, 120, true),
                textColumn(cols[5], b -> ReservationGuiController.formatCurrency(b.getTotalPrice()), 135, true),
                bookingStatusBadgeColumn(cols[6], 130),
                paymentStatusBadgeColumn(cols[7], 130)
        ));
    }

    private void configurePassengerTable() {
        configureExplorerTable(passengerTable, "No passengers registered in the system.");

        String[] cols = ReservationGuiController.PASSENGER_TABLE_COLUMNS;
        passengerTable.getColumns().setAll(List.of(
                textColumn(cols[0], Passenger::getId, 100, true),
                textColumn(cols[1], Passenger::getFullName, 160, false),
                textColumn(cols[2], Passenger::getEmail, 190, false),
                textColumn(cols[3], Passenger::getPhoneNumber, 120, true),
                textColumn(cols[4], p -> p.getDateOfBirth().toString(), 115, true),
                textColumn(cols[5], Passenger::getGender, 85, false),
                textColumn(cols[6], Passenger::getPassportOrId, 125, true),
                textColumn(cols[7], p -> String.valueOf(p.getBookingHistory().size()), 90, true)
        ));
    }

    private void populateCatalogSelectors() {
        List<Airport> airports = controller.getAirports();
        departureCombo.getItems().setAll(airports);
        arrivalCombo.getItems().setAll(airports);
        aircraftCombo.getItems().setAll(controller.getAircrafts());
    }

    private void suggestNextFlightIdentity() {
        int nextIndex = controller.getAllFlights().size() + 1;
        flightIdField.setText(String.format(Locale.ROOT, "FL-%02d", nextIndex));
        flightNumberField.setText(String.format(Locale.ROOT, "VN%d", 100 + nextIndex * 2));
    }

    private void resetFlightFormDefaults() {
        suggestNextFlightIdentity();
        selectDefaultAirportPair(departureCombo, arrivalCombo);
        if (!aircraftCombo.getItems().isEmpty()) {
            aircraftCombo.getSelectionModel().select(0);
        }

        String defaultDate = controller.getDefaultTravelDate().plusDays(2).toString();
        departureTimeField.setText(defaultDate + " 09:00");
        arrivalTimeField.setText(defaultDate + " 11:15");
        basePriceField.setText("1500000");
    }

    private void handleScheduleFlight() {
        Airport dep = departureCombo.getSelectionModel().getSelectedItem();
        Airport arr = arrivalCombo.getSelectionModel().getSelectedItem();
        Aircraft aircraft = aircraftCombo.getSelectionModel().getSelectedItem();

        try {
            Flight created = controller.scheduleFlight(
                    flightIdField.getText(),
                    flightNumberField.getText(),
                    dep == null ? "" : dep.getAirportCode(),
                    arr == null ? "" : arr.getAirportCode(),
                    departureTimeField.getText(),
                    arrivalTimeField.getText(),
                    aircraft == null ? "" : aircraft.getAircraftId(),
                    basePriceField.getText()
            );
            refreshAllTablesAndMetrics();
            flightTable.getSelectionModel().select(created);
            flightTable.scrollTo(created);
            resetFlightFormDefaults();
            showInlineSuccess(
                    "FLIGHT SCHEDULED",
                    "Flight " + created.getFlightNumber() + " (" + created.getFlightId() + ") "
                            + ReservationGuiController.formatRoute(created) + " scheduled with "
                            + created.getAvailableSeats().size() + " cabin seats."
            );
        } catch (AirlineReservationException ex) {
            showInlineError("SCHEDULE ERROR", ex.getMessage());
        }
    }

    private void handleStatusTransition(FlightStatus targetStatus) {
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInlineWarning("SELECT FLIGHT", "Please select a flight from the table first.");
            return;
        }
        try {
            Flight updated = controller.updateFlightStatus(selected.getFlightId(), targetStatus);
            refreshAllTablesAndMetrics();
            showInlineSuccess(
                    "STATUS UPDATED",
                    "Flight " + updated.getFlightNumber() + " (" + updated.getFlightId() + ") transitioned to "
                            + updated.getStatus() + "."
            );
        } catch (AirlineReservationException ex) {
            showInlineError("TRANSITION FAILED", ex.getMessage());
        }
    }

    private void handleArriveFlight() {
        Window owner = ownerWindow();
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInlineWarning("SELECT FLIGHT", "Please select a flight from the table first.");
            return;
        }

        if (selected.getStatus() == FlightStatus.DEPARTED) {
            boolean confirmed = ArbitrumTheme.showConfirmation(
                    owner,
                    "Confirm Flight Arrival",
                    "Mark flight " + selected.getFlightNumber() + " (" + selected.getFlightId() + ") as ARRIVED?\n"
                            + "Checked-in bookings will transition to COMPLETED, and unpaid PENDING bookings will be CANCELLED with seats released."
            );
            if (!confirmed) {
                return;
            }
        }

        try {
            Flight arrived = controller.arriveFlight(selected.getFlightId());
            refreshAllTablesAndMetrics();
            showInlineSuccess(
                    "FLIGHT ARRIVED",
                    "Flight " + arrived.getFlightNumber() + " (" + arrived.getFlightId()
                            + ") marked ARRIVED — checked-in bookings completed and unpaid holds released."
            );
        } catch (AirlineReservationException ex) {
            showInlineError("ARRIVAL FAILED", ex.getMessage());
        }
    }

    private void handleCancelFlight() {
        Window owner = ownerWindow();
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInlineWarning("SELECT FLIGHT", "Please select a flight from the table first.");
            return;
        }

        if (selected.getStatus() == FlightStatus.SCHEDULED || selected.getStatus() == FlightStatus.BOARDING) {
            boolean confirmed = ArbitrumTheme.showConfirmation(
                    owner,
                    "Confirm Flight Cancellation",
                    "Cancel flight " + selected.getFlightNumber() + " (" + selected.getFlightId() + ")?\n"
                            + "All active bookings on this flight will be CANCELLED, seats released, and completed payments REFUNDED."
            );
            if (!confirmed) {
                return;
            }
        }

        try {
            Flight cancelled = controller.cancelFlight(selected.getFlightId());
            refreshAllTablesAndMetrics();
            showInlineSuccess(
                    "FLIGHT CANCELLED",
                    "Flight " + cancelled.getFlightNumber() + " (" + cancelled.getFlightId()
                            + ") CANCELLED — active bookings cancelled and completed payments marked REFUNDED."
            );
        } catch (AirlineReservationException ex) {
            showInlineError("CANCELLATION FAILED", ex.getMessage());
        }
    }

    private void refreshAllTablesAndMetrics() {
        if (!controller.isAdminLoggedIn()) {
            flightTable.getItems().clear();
            bookingTable.getItems().clear();
            passengerTable.getItems().clear();
            flightCountLabel.setText("Showing 0 flight(s)");
            adminBookingCountLabel.setText("Showing 0 booking(s)");
            passengerCountLabel.setText("Showing 0 passenger(s)");
            totalFlightsMetricLabel.setText("0");
            totalBookingsMetricLabel.setText("0");
            registeredPassengersMetricLabel.setText("0");
            settledRevenueMetricLabel.setText(ReservationGuiController.formatCurrency(0));
            updateFlightLifecycleButtons(null);
            return;
        }

        List<Flight> flights = controller.getAdminFlights();
        refreshTableItems(flightTable, flights, Flight::getFlightId);
        if (flightTable.getSelectionModel().getSelectedItem() == null && !flights.isEmpty()) {
            flightTable.getSelectionModel().selectFirst();
        }
        flightCountLabel.setText("Showing " + flights.size() + " flight(s)");
        updateFlightLifecycleButtons(flightTable.getSelectionModel().getSelectedItem());

        List<Booking> bookings = controller.getAllBookings();
        refreshTableItems(bookingTable, bookings, Booking::getBookingId);
        adminBookingCountLabel.setText("Showing " + bookings.size() + " booking(s)");

        List<Passenger> passengers = controller.getAllPassengers();
        refreshTableItems(passengerTable, passengers, Passenger::getId);
        passengerCountLabel.setText("Showing " + passengers.size() + " passenger(s)");

        ReservationGuiController.AdminMetrics metrics = controller.getAdminMetrics();
        totalFlightsMetricLabel.setText(String.valueOf(metrics.totalFlightsCount()));
        totalBookingsMetricLabel.setText(String.valueOf(metrics.totalBookingsCount()));
        registeredPassengersMetricLabel.setText(String.valueOf(metrics.registeredPassengersCount()));
        settledRevenueMetricLabel.setText(ReservationGuiController.formatCurrency(metrics.settledRevenue()));
    }

    private static void configureAircraftCombo(ComboBox<Aircraft> comboBox) {
        ArbitrumTheme.styleComboBox(comboBox);
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Aircraft aircraft) {
                return aircraft == null ? "" : aircraft.getAircraftId() + " · " + aircraft.getModelInfo();
            }

            @Override
            public Aircraft fromString(String string) {
                return null;
            }
        });
    }

    private static VBox labeledField(String labelText, Node control) {
        return new VBox(4, ArbitrumTheme.createFieldLabel(labelText), control);
    }

    private static Label createAdminStatValueLabel() {
        Label label = new Label("0");
        label.getStyleClass().add("admin-stat-value");
        return label;
    }

    private static VBox buildAdminStatTile(String caption, Label valueLabel) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("admin-stat-label");
        VBox tile = new VBox(4, captionLabel, valueLabel);
        tile.getStyleClass().add("admin-stat-card");
        HBox.setHgrow(tile, Priority.ALWAYS);
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }
}
