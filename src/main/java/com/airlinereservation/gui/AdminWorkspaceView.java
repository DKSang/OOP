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

    private final Label totalFlightsMetricLabel = createAdminStatValueLabel();
    private final Label totalBookingsMetricLabel = createAdminStatValueLabel();
    private final Label registeredPassengersMetricLabel = createAdminStatValueLabel();
    private final Label settledRevenueMetricLabel = createAdminStatValueLabel();

    private final TableView<Booking> bookingTable = new TableView<>();
    private final Label adminBookingCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 booking(s)");

    private final TableView<Passenger> passengerTable = new TableView<>();
    private final Label passengerCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 passenger(s)");

    private final Tab flightOperationsTab;
    private final Tab monitorTab;

    public AdminWorkspaceView(ReservationGuiController controller) {
        super(
                controller,
                "ADMIN CONSOLE // OPERATIONS",
                "Admin Operations Workspace",
                "Schedule flights, orchestrate lifecycle cascades, and monitor all system bookings and passengers."
        );
        this.flightOperationsTab = createNonClosableTab("Flight Operations", createFlightOperationsTab());
        this.monitorTab = createNonClosableTab("All Bookings & Passengers", createMonitorTab());
        tabs.getTabs().addAll(flightOperationsTab, monitorTab);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == flightOperationsTab || newTab == monitorTab) {
                refreshAllTablesAndMetrics();
            }
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
        return "Operations Console — " + user.getFullName() + " (" + user.getId() + ")";
    }

    private BorderPane createFlightOperationsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(ArbitrumTheme.SPACE_8, 0, 0, 0));

        VBox formCard = createScheduleFlightCard();
        BorderPane.setMargin(formCard, new Insets(0, 0, ArbitrumTheme.SPACE_16, 0));

        configureFlightTable();
        BorderPane actionBar = createFlightActionBar();
        BorderPane.setMargin(actionBar, new Insets(ArbitrumTheme.SPACE_16, 0, 0, 0));

        layout.setTop(formCard);
        layout.setCenter(flightTable);
        layout.setBottom(actionBar);
        return layout;
    }

    private VBox createScheduleFlightCard() {
        VBox card = new VBox(ArbitrumTheme.SPACE_12);
        card.getStyleClass().add("card");

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("FLIGHT DISPATCH // SCHEDULE CONTROL");
        Label sectionTitle = ArbitrumTheme.createHeadingLabel("Schedule New Flight", "heading-md");

        GridPane fieldsGrid = new GridPane();
        fieldsGrid.setHgap(ArbitrumTheme.SPACE_16);
        fieldsGrid.setVgap(ArbitrumTheme.SPACE_12);

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
        fieldsGrid.add(labeledField("Departure Airport", departureCombo), 2, 0);
        fieldsGrid.add(labeledField("Arrival Airport", arrivalCombo), 3, 0);

        fieldsGrid.add(labeledField("Operating Aircraft", aircraftCombo), 0, 1);
        fieldsGrid.add(labeledField("Departure (YYYY-MM-DD HH:MM)", departureTimeField), 1, 1);
        fieldsGrid.add(labeledField("Arrival (YYYY-MM-DD HH:MM)", arrivalTimeField), 2, 1);
        fieldsGrid.add(labeledField("Base Price (VND)", basePriceField), 3, 1);

        Button scheduleButton = ArbitrumTheme.createPrimaryButton("Schedule Flight");
        scheduleButton.setOnAction(e -> handleScheduleFlight());

        Button clearButton = ArbitrumTheme.createSecondaryButton("Clear Form");
        clearButton.setOnAction(e -> resetFlightFormDefaults());

        HBox actionsRow = new HBox(ArbitrumTheme.SPACE_12, scheduleButton, clearButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(actionsRow, new Insets(4, 0, 0, 0));

        card.getChildren().addAll(eyebrow, sectionTitle, fieldsGrid, actionsRow);
        return card;
    }

    private void configureFlightTable() {
        configureExplorerTable(flightTable, "No flights scheduled in the system.");
        flightTable.setMinHeight(240);
        flightTable.setPrefHeight(290);
        flightTable.getColumns().setAll(buildFlightExplorerColumns());
    }

    private BorderPane createFlightActionBar() {
        BorderPane bar = new BorderPane();

        Label tipLabel = ArbitrumTheme.createMutedLabel(
                "Select a flight row to transition status or trigger arrival/cancellation cascades."
        );
        VBox leftMeta = new VBox(2, flightCountLabel, tipLabel);
        leftMeta.setAlignment(Pos.CENTER_LEFT);

        Button boardingButton = ArbitrumTheme.createSecondaryButton("Mark Boarding");
        boardingButton.setOnAction(e -> handleStatusTransition(FlightStatus.BOARDING));

        Button departedButton = ArbitrumTheme.createSecondaryButton("Mark Departed");
        departedButton.setOnAction(e -> handleStatusTransition(FlightStatus.DEPARTED));

        Button arrivedButton = ArbitrumTheme.createPrimaryButton("Mark Arrived");
        arrivedButton.setOnAction(e -> handleArriveFlight());

        Button cancelButton = new Button("Cancel Flight");
        cancelButton.getStyleClass().add("btn-danger-pill");
        cancelButton.setOnAction(e -> handleCancelFlight());

        HBox actions = new HBox(ArbitrumTheme.SPACE_12, boardingButton, departedButton, arrivedButton, cancelButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bar.setLeft(leftMeta);
        bar.setRight(actions);
        return bar;
    }

    private VBox createMonitorTab() {
        VBox layout = new VBox(ArbitrumTheme.SPACE_16);
        layout.setPadding(new Insets(ArbitrumTheme.SPACE_8, 0, 0, 0));

        VBox statsSection = createOnchainStatsSection();

        configureAdminBookingTable();
        VBox bookingsSection = createTableSection(
                "BOOKING LEDGER // SYSTEM-WIDE",
                "All Bookings",
                adminBookingCountLabel,
                bookingTable
        );

        configurePassengerTable();
        VBox passengersSection = createTableSection(
                "PASSENGER REGISTRY // VERIFIED PROFILES",
                "Registered Passengers",
                passengerCountLabel,
                passengerTable
        );

        layout.getChildren().addAll(statsSection, bookingsSection, passengersSection);
        return layout;
    }

    private VBox createOnchainStatsSection() {
        VBox section = new VBox(ArbitrumTheme.SPACE_12);
        section.getStyleClass().add("card-stone");

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("THE NETWORK STANDARD // LIVE TELEMETRY");
        Label heading = ArbitrumTheme.createHeadingLabel("System-Wide Overview", "heading-md");

        HBox tilesRow = new HBox(
                ArbitrumTheme.SPACE_12,
                buildAdminStatTile("TOTAL FLIGHTS", totalFlightsMetricLabel),
                buildAdminStatTile("TOTAL BOOKINGS", totalBookingsMetricLabel),
                buildAdminStatTile("REGISTERED PASSENGERS", registeredPassengersMetricLabel),
                buildAdminStatTile("SETTLED REVENUE", settledRevenueMetricLabel)
        );

        section.getChildren().addAll(eyebrow, heading, tilesRow);
        return section;
    }

    private VBox createTableSection(String eyebrowText, String headingText, Label countLabel, TableView<?> table) {
        Label eyebrow = ArbitrumTheme.createEyebrowLabel(eyebrowText);
        Label heading = ArbitrumTheme.createHeadingLabel(headingText, "heading-md");
        VBox titleBlock = new VBox(2, eyebrow, heading);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(ArbitrumTheme.SPACE_12, titleBlock, spacer, countLabel);
        headerRow.setAlignment(Pos.BOTTOM_LEFT);

        VBox section = new VBox(ArbitrumTheme.SPACE_8, headerRow, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return section;
    }

    private void configureAdminBookingTable() {
        configureExplorerTable(bookingTable, "No bookings recorded in the system.");
        bookingTable.setMinHeight(210);
        bookingTable.setPrefHeight(240);

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
        passengerTable.setMinHeight(190);
        passengerTable.setPrefHeight(220);

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

    private void resetFlightFormDefaults() {
        flightIdField.clear();
        flightNumberField.clear();
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
        Window owner = ownerWindow();
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
            resetFlightFormDefaults();
            ArbitrumTheme.showInfo(
                    owner,
                    "Flight Scheduled",
                    "Flight " + created.getFlightNumber() + " (" + created.getFlightId() + ") "
                            + ReservationGuiController.formatRoute(created) + " has been scheduled with "
                            + created.getAvailableSeats().size() + " available seats."
            );
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Schedule Flight Failed", ex.getMessage());
        }
    }

    private void handleStatusTransition(FlightStatus targetStatus) {
        Window owner = ownerWindow();
        Flight selected = requireSelectedFlight(owner);
        if (selected == null) {
            return;
        }
        try {
            Flight updated = controller.updateFlightStatus(selected.getFlightId(), targetStatus);
            refreshAllTablesAndMetrics();
            ArbitrumTheme.showInfo(
                    owner,
                    "Flight Status Updated",
                    "Flight " + updated.getFlightNumber() + " (" + updated.getFlightId() + ") is now "
                            + updated.getStatus() + "."
            );
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Status Update Failed", ex.getMessage());
        }
    }

    private void handleArriveFlight() {
        Window owner = ownerWindow();
        Flight selected = requireSelectedFlight(owner);
        if (selected == null) {
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
            ArbitrumTheme.showInfo(
                    owner,
                    "Flight Arrived",
                    "Flight " + arrived.getFlightNumber() + " (" + arrived.getFlightId() + ") is now ARRIVED.\n"
                            + "Associated bookings and seats have been updated."
            );
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Flight Arrival Failed", ex.getMessage());
        }
    }

    private void handleCancelFlight() {
        Window owner = ownerWindow();
        Flight selected = requireSelectedFlight(owner);
        if (selected == null) {
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
            ArbitrumTheme.showInfo(
                    owner,
                    "Flight Cancelled",
                    "Flight " + cancelled.getFlightNumber() + " (" + cancelled.getFlightId() + ") has been CANCELLED.\n"
                            + "Affected bookings were cancelled and completed payments were marked REFUNDED."
            );
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Flight Cancellation Failed", ex.getMessage());
        }
    }

    private Flight requireSelectedFlight(Window owner) {
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ArbitrumTheme.showWarning(owner, "No Flight Selected", "Please select a flight from the table first.");
        }
        return selected;
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
            return;
        }

        List<Flight> flights = controller.getAdminFlights();
        refreshTableItems(flightTable, flights, Flight::getFlightId);
        flightCountLabel.setText("Showing " + flights.size() + " flight(s)");

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
        return new VBox(ArbitrumTheme.SPACE_8, ArbitrumTheme.createFieldLabel(labelText), control);
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
