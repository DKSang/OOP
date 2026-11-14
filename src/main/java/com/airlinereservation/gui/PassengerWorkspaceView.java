package com.airlinereservation.gui;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableRow;
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

import java.util.List;

/**
 * Role workspace container for authenticated {@code Passenger} sessions.
 */
public class PassengerWorkspaceView extends AbstractWorkspaceView {

    private final ComboBox<Airport> departureCombo = new ComboBox<>();
    private final ComboBox<Airport> arrivalCombo = new ComboBox<>();
    private final DatePicker travelDatePicker;
    private final TextField seatCountField = new TextField("1");
    private final Label flightCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 flight(s)");
    private final TableView<Flight> flightTable = new TableView<>();

    private final Label bookingCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 booking(s)");
    private final TableView<Booking> bookingTable = new TableView<>();

    private final Tab searchFlightsTab;
    private final Tab myBookingsTab;

    private List<Flight> displayedFlights = List.of();

    public PassengerWorkspaceView(ReservationGuiController controller) {
        super(
                controller,
                "PASSENGER PORTAL // FLIGHT EXPLORER",
                "Passenger Workspace",
                "Search flights, reserve cabin seats, settle bookings, check in, or manage cancellations."
        );
        this.travelDatePicker = ArbitrumTheme.createIsoDatePicker(controller.getDefaultTravelDate());

        this.searchFlightsTab = createNonClosableTab("Search & Book Flights", createSearchFlightsTab());
        this.myBookingsTab = createNonClosableTab("My Bookings", createMyBookingsTab());
        tabs.getTabs().addAll(searchFlightsTab, myBookingsTab);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == myBookingsTab) {
                refreshBookingTable();
            } else if (newTab == searchFlightsTab) {
                flightTable.refresh();
            }
        });
    }

    @Override
    public void refreshData() {
        super.refreshData();
        populateAirportSelectors();
        resetFiltersAndShowAllScheduled();
        refreshBookingTable();
    }

    @Override
    protected String formatWelcomeTitle(Person user) {
        return "Welcome, " + user.getFullName() + " (" + user.getId() + ")";
    }

    private BorderPane createSearchFlightsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(ArbitrumTheme.SPACE_8, 0, 0, 0));

        VBox filterCard = createSearchFilterCard();
        BorderPane.setMargin(filterCard, new Insets(0, 0, ArbitrumTheme.SPACE_16, 0));

        configureFlightTable();
        BorderPane bottomBar = createBottomActionBar();
        BorderPane.setMargin(bottomBar, new Insets(ArbitrumTheme.SPACE_16, 0, 0, 0));

        layout.setTop(filterCard);
        layout.setCenter(flightTable);
        layout.setBottom(bottomBar);
        return layout;
    }

    private VBox createSearchFilterCard() {
        VBox card = new VBox(ArbitrumTheme.SPACE_12);
        card.getStyleClass().add("card");

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("FLIGHT SEARCH // SCHEDULE QUERY");
        Label sectionTitle = ArbitrumTheme.createHeadingLabel("Find Available Flights", "heading-md");

        GridPane fieldsGrid = new GridPane();
        fieldsGrid.setHgap(ArbitrumTheme.SPACE_16);
        fieldsGrid.setVgap(ArbitrumTheme.SPACE_8);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            fieldsGrid.getColumnConstraints().add(col);
        }

        configureAirportCombo(departureCombo);
        configureAirportCombo(arrivalCombo);
        ArbitrumTheme.styleTextInput(seatCountField);
        seatCountField.setOnAction(e -> executeFlightSearch());

        fieldsGrid.add(ArbitrumTheme.createFieldLabel("Departure Airport"), 0, 0);
        fieldsGrid.add(ArbitrumTheme.createFieldLabel("Arrival Airport"), 1, 0);
        fieldsGrid.add(ArbitrumTheme.createFieldLabel("Travel Date (YYYY-MM-DD)"), 2, 0);
        fieldsGrid.add(ArbitrumTheme.createFieldLabel("Seats Needed"), 3, 0);

        fieldsGrid.add(departureCombo, 0, 1);
        fieldsGrid.add(arrivalCombo, 1, 1);
        fieldsGrid.add(travelDatePicker, 2, 1);
        fieldsGrid.add(seatCountField, 3, 1);

        Button searchButton = ArbitrumTheme.createPrimaryButton("Search Flights");
        searchButton.setOnAction(e -> executeFlightSearch());

        Button resetButton = ArbitrumTheme.createSecondaryButton("Show All Scheduled Flights");
        resetButton.setOnAction(e -> resetFiltersAndShowAllScheduled());

        HBox actionsRow = new HBox(ArbitrumTheme.SPACE_12, searchButton, resetButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(actionsRow, new Insets(4, 0, 0, 0));

        card.getChildren().addAll(eyebrow, sectionTitle, fieldsGrid, actionsRow);
        return card;
    }

    private void configureFlightTable() {
        configureExplorerTable(flightTable, "No matching flights found.");
        flightTable.getColumns().setAll(buildFlightExplorerColumns());

        flightTable.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openSeatSelectionDialog();
                }
            });
            return row;
        });
    }

    private BorderPane createBottomActionBar() {
        BorderPane bar = new BorderPane();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button selectSeatsButton = ArbitrumTheme.createPrimaryButton("Select Seats & Book");
        selectSeatsButton.setOnAction(e -> openSeatSelectionDialog());

        Label tipLabel = ArbitrumTheme.createMutedLabel("Tip: Double-click any flight row or select and press the button.");
        VBox leftMeta = new VBox(2, flightCountLabel, tipLabel);
        leftMeta.setAlignment(Pos.CENTER_LEFT);

        bar.setLeft(leftMeta);
        bar.setRight(selectSeatsButton);
        return bar;
    }

    private void populateAirportSelectors() {
        List<Airport> airports = controller.getAirports();
        departureCombo.getItems().setAll(airports);
        arrivalCombo.getItems().setAll(airports);
        selectDefaultAirportPair(departureCombo, arrivalCombo);
    }

    private void executeFlightSearch() {
        Airport dep = departureCombo.getSelectionModel().getSelectedItem();
        Airport arr = arrivalCombo.getSelectionModel().getSelectedItem();
        String depCode = dep == null ? "" : dep.getAirportCode();
        String arrCode = arr == null ? "" : arr.getAirportCode();

        try {
            List<Flight> matches = controller.searchFlights(
                    depCode,
                    arrCode,
                    travelDatePicker.getEditor().getText(),
                    seatCountField.getText()
            );
            populateFlightTable(matches);
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(
                    getScene() == null ? null : getScene().getWindow(),
                    "Flight Search Error",
                    ex.getMessage()
            );
        }
    }

    private void resetFiltersAndShowAllScheduled() {
        selectDefaultAirportPair(departureCombo, arrivalCombo);
        travelDatePicker.setValue(controller.getDefaultTravelDate());
        seatCountField.setText("1");
        populateFlightTable(controller.getScheduledFlights());
    }

    private void populateFlightTable(List<Flight> flights) {
        this.displayedFlights = List.copyOf(flights);
        flightTable.getItems().setAll(displayedFlights);
        flightCountLabel.setText("Showing " + displayedFlights.size() + " flight(s)");
    }

    private void openSeatSelectionDialog() {
        Window owner = getScene() == null ? null : getScene().getWindow();
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ArbitrumTheme.showWarning(
                    owner,
                    "No Flight Selected",
                    "Please select a flight from the table first."
            );
            return;
        }

        try {
            SeatSelectionDialog dialog = new SeatSelectionDialog(owner, controller, selected);
            ArbitrumTheme.showModalWithBackdrop(owner, dialog);
            if (dialog.getCreatedBooking() != null) {
                refreshTablesAfterMutation();
            }
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Unable to Open Seat Map", ex.getMessage());
        }
    }

    private BorderPane createMyBookingsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(ArbitrumTheme.SPACE_8, 0, 0, 0));

        VBox headerCard = new VBox(
                ArbitrumTheme.SPACE_8,
                ArbitrumTheme.createEyebrowLabel("BOOKING HISTORY // PERSONAL LEDGER"),
                ArbitrumTheme.createHeadingLabel("My Bookings", "heading-md"),
                ArbitrumTheme.createMutedLabel(
                        "Inspect bookings, settle pending payments (with failure-retry simulation), check in, or cancel."
                )
        );
        headerCard.getStyleClass().add("card-stone");
        BorderPane.setMargin(headerCard, new Insets(0, 0, ArbitrumTheme.SPACE_16, 0));

        configureBookingTable();
        BorderPane actionBar = createBookingActionBar();
        BorderPane.setMargin(actionBar, new Insets(ArbitrumTheme.SPACE_16, 0, 0, 0));

        layout.setTop(headerCard);
        layout.setCenter(bookingTable);
        layout.setBottom(actionBar);
        return layout;
    }

    private void configureBookingTable() {
        configureExplorerTable(bookingTable, "You have no bookings yet. Reserve seats from Search & Book Flights.");

        String[] cols = ReservationGuiController.BOOKING_TABLE_COLUMNS;
        bookingTable.getColumns().setAll(List.of(
                textColumn(cols[0], Booking::getBookingId, 95, true),
                textColumn(cols[1], b -> b.getFlight().getFlightNumber(), 95, true),
                textColumn(cols[2], b -> ReservationGuiController.formatRoute(b.getFlight()), 120, true),
                textColumn(cols[3], ReservationGuiController::formatSeatNumbers, 130, true),
                textColumn(cols[4], b -> ReservationGuiController.formatCurrency(b.getTotalPrice()), 140, true),
                bookingStatusBadgeColumn(cols[5], 135),
                paymentStatusBadgeColumn(cols[6], 135)
        ));
    }

    private BorderPane createBookingActionBar() {
        BorderPane bar = new BorderPane();

        Label tipLabel = ArbitrumTheme.createMutedLabel("Select a booking row to pay, check in, or cancel.");
        VBox leftMeta = new VBox(2, bookingCountLabel, tipLabel);
        leftMeta.setAlignment(Pos.CENTER_LEFT);

        Button payButton = ArbitrumTheme.createPrimaryButton("Pay Booking");
        payButton.setOnAction(e -> openPaymentDialog());

        Button checkInButton = ArbitrumTheme.createSecondaryButton("Check-in");
        checkInButton.setOnAction(e -> checkInSelectedBooking());

        Button cancelButton = ArbitrumTheme.createSecondaryButton("Cancel Booking");
        cancelButton.setOnAction(e -> cancelSelectedBooking());

        HBox actions = new HBox(ArbitrumTheme.SPACE_12, payButton, checkInButton, cancelButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bar.setLeft(leftMeta);
        bar.setRight(actions);
        return bar;
    }

    private void refreshBookingTable() {
        if (!controller.isPassengerLoggedIn()) {
            bookingTable.getItems().clear();
            bookingCountLabel.setText("Showing 0 booking(s)");
            return;
        }
        List<Booking> bookings = controller.getPassengerBookings();
        refreshTableItems(bookingTable, bookings, Booking::getBookingId);
        bookingCountLabel.setText("Showing " + bookings.size() + " booking(s)");
    }

    private void refreshTablesAfterMutation() {
        flightTable.refresh();
        populateFlightTable(displayedFlights);
        refreshBookingTable();
    }

    private Booking getSelectedBooking() {
        return bookingTable.getSelectionModel().getSelectedItem();
    }

    private Booking requireSelectedBooking(Window owner, String warningMessage) {
        Booking selected = getSelectedBooking();
        if (selected == null) {
            ArbitrumTheme.showWarning(owner, "No Booking Selected", warningMessage);
        }
        return selected;
    }

    private void openPaymentDialog() {
        Window owner = ownerWindow();
        Booking selected = requireSelectedBooking(owner, "Please select a booking from the table first.");
        if (selected == null) {
            return;
        }

        try {
            Booking payable = controller.validateBookingPayable(selected.getBookingId());
            PaymentDialog dialog = new PaymentDialog(owner, controller, payable, this::refreshTablesAfterMutation);
            ArbitrumTheme.showModalWithBackdrop(owner, dialog);
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Payment Unavailable", ex.getMessage());
        }
    }

    private void checkInSelectedBooking() {
        Window owner = ownerWindow();
        Booking selected = requireSelectedBooking(owner, "Please select a confirmed booking to check in.");
        if (selected == null) {
            return;
        }

        try {
            Booking updated = controller.checkInBooking(selected.getBookingId());
            refreshTablesAfterMutation();
            ArbitrumTheme.showInfo(
                    owner,
                    "Check-in Successful",
                    "Booking " + updated.getBookingId() + " is now " + updated.getStatus()
                            + " for flight " + updated.getFlight().getFlightNumber() + "."
            );
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Check-in Failed", ex.getMessage());
        }
    }

    private void cancelSelectedBooking() {
        Window owner = ownerWindow();
        Booking selected = requireSelectedBooking(owner, "Please select a booking to cancel.");
        if (selected == null) {
            return;
        }

        if (selected.getStatus() != BookingStatus.PENDING && selected.getStatus() != BookingStatus.CONFIRMED) {
            ArbitrumTheme.showError(
                    owner,
                    "Cancellation Unavailable",
                    "Cannot cancel booking " + selected.getBookingId() + " in status: " + selected.getStatus()
            );
            return;
        }

        boolean hasCompletedPayment = selected.getPayment() != null
                && selected.getPayment().getStatus() == PaymentStatus.COMPLETED;
        String refundNote = hasCompletedPayment
                ? "\nCompleted payment " + selected.getPayment().getPaymentId() + " will be REFUNDED."
                : "";
        boolean confirmed = ArbitrumTheme.showConfirmation(
                owner,
                "Cancel Booking",
                "Cancel booking " + selected.getBookingId()
                        + " (" + selected.getFlight().getFlightNumber()
                        + ", seats: " + ReservationGuiController.formatSeatNumbers(selected) + ")?\n"
                        + "Reserved/occupied seats will be released back to AVAILABLE." + refundNote
        );
        if (!confirmed) {
            return;
        }

        try {
            controller.cancelBooking(selected.getBookingId());
            refreshTablesAfterMutation();
            ArbitrumTheme.showInfo(
                    owner,
                    "Booking Cancelled",
                    "Booking " + selected.getBookingId() + " has been CANCELLED and its seats are now AVAILABLE."
                            + (hasCompletedPayment ? "\nPayment status is now REFUNDED." : "")
            );
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(owner, "Cancellation Failed", ex.getMessage());
        }
    }
}
