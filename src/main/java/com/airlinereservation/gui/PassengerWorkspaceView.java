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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private final Label flightSelectionHintLabel = ArbitrumTheme.createMutedLabel(
            "Double-click any flight row or select a flight to open the cabin seat map."
    );
    private final Button selectSeatsButton = ArbitrumTheme.createPrimaryButton("Select Cabin Seats");
    private final TableView<Flight> flightTable = new TableView<>();

    private final Label bookingCountLabel = ArbitrumTheme.createMonoLabel("Showing 0 booking(s)");
    private final Label bookingSelectionHintLabel = ArbitrumTheme.createMutedLabel(
            "Select a booking row to settle payment, check in, or cancel."
    );
    private final Button payBookingButton = ArbitrumTheme.createPrimaryButton("Pay Booking");
    private final Button checkInButton = ArbitrumTheme.createSecondaryButton("Check In");
    private final Button cancelBookingButton = new Button("Cancel Booking");
    private final TableView<Booking> bookingTable = new TableView<>();

    private final Tab searchFlightsTab;
    private final Tab myBookingsTab;

    private List<Flight> displayedFlights = List.of();

    public PassengerWorkspaceView(ReservationGuiController controller) {
        super(
                controller,
                "PASSENGER WORKSPACE",
                "Passenger Workspace",
                "Search scheduled routes, choose seats on the interactive aircraft map, and manage your bookings."
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
        return user.getFullName() + " · " + user.getId();
    }

    private BorderPane createSearchFlightsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(4, 0, 0, 0));

        VBox filterCard = createSearchFilterCard();
        BorderPane.setMargin(filterCard, new Insets(0, 0, ArbitrumTheme.SPACE_12, 0));

        configureFlightTable();
        BorderPane bottomBar = createFlightActionBar();
        BorderPane.setMargin(bottomBar, new Insets(ArbitrumTheme.SPACE_12, 0, 0, 0));

        layout.setTop(filterCard);
        layout.setCenter(flightTable);
        layout.setBottom(bottomBar);
        return layout;
    }

    private VBox createSearchFilterCard() {
        VBox card = new VBox(ArbitrumTheme.SPACE_12);
        card.getStyleClass().add("card");

        configureAirportCombo(departureCombo);
        configureAirportCombo(arrivalCombo);
        ArbitrumTheme.styleTextInput(seatCountField);
        seatCountField.setPrefWidth(90);
        seatCountField.setOnAction(e -> executeFlightSearch());

        Button swapButton = new Button("⇄");
        swapButton.getStyleClass().add("btn-icon-swap");
        swapButton.setTooltip(new Tooltip("Swap departure and arrival airports"));
        swapButton.setOnAction(e -> swapAirportSelection(departureCombo, arrivalCombo));

        VBox swapWrapper = new VBox(ArbitrumTheme.SPACE_8, new Label(""), swapButton);
        swapWrapper.setAlignment(Pos.BOTTOM_CENTER);

        VBox depGroup = labeledInput("From", departureCombo);
        VBox arrGroup = labeledInput("To", arrivalCombo);
        VBox dateGroup = labeledInput("Departure Date", travelDatePicker);
        VBox seatsGroup = labeledInput("Passengers", seatCountField);

        HBox.setHgrow(depGroup, Priority.ALWAYS);
        HBox.setHgrow(arrGroup, Priority.ALWAYS);
        dateGroup.setPrefWidth(175);
        seatsGroup.setPrefWidth(105);

        Button searchButton = ArbitrumTheme.createPrimaryButton("Search Flights");
        searchButton.setOnAction(e -> executeFlightSearch());

        Button resetButton = ArbitrumTheme.createSecondaryButton("All Scheduled");
        resetButton.setOnAction(e -> {
            hideInlineNotice();
            resetFiltersAndShowAllScheduled();
        });

        HBox buttonGroup = new HBox(ArbitrumTheme.SPACE_8, searchButton, resetButton);
        buttonGroup.setAlignment(Pos.BOTTOM_LEFT);
        VBox actionsWrapper = new VBox(ArbitrumTheme.SPACE_8, new Label(""), buttonGroup);
        actionsWrapper.setAlignment(Pos.BOTTOM_LEFT);

        HBox filterRow = new HBox(
                ArbitrumTheme.SPACE_12,
                depGroup,
                swapWrapper,
                arrGroup,
                dateGroup,
                seatsGroup,
                actionsWrapper
        );
        filterRow.setAlignment(Pos.BOTTOM_LEFT);

        card.getChildren().add(filterRow);
        return card;
    }

    private void configureFlightTable() {
        configureExplorerTable(flightTable, "No matching flights found. Try another date or click 'All Scheduled'.");
        flightTable.getColumns().setAll(buildFlightExplorerColumns());

        flightTable.getSelectionModel().selectedItemProperty().addListener((obs, oldFlight, newFlight) -> {
            updateFlightActionState(newFlight);
        });

        flightTable.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openSeatSelectionDialog();
                }
            });
            return row;
        });

        updateFlightActionState(null);
    }

    private BorderPane createFlightActionBar() {
        BorderPane bar = new BorderPane();
        bar.getStyleClass().add("action-footer-bar");

        selectSeatsButton.setOnAction(e -> openSeatSelectionDialog());

        VBox leftMeta = new VBox(2, flightCountLabel, flightSelectionHintLabel);
        leftMeta.setAlignment(Pos.CENTER_LEFT);

        bar.setLeft(leftMeta);
        bar.setRight(selectSeatsButton);
        return bar;
    }

    private void updateFlightActionState(Flight selected) {
        if (selected == null) {
            selectSeatsButton.setDisable(true);
            selectSeatsButton.setText("Select Cabin Seats");
            flightSelectionHintLabel.setText("Double-click any flight row or select a flight to open the cabin seat map.");
            return;
        }
        boolean canBook = selected.canAcceptNewBooking() && !selected.getAvailableSeats().isEmpty();
        selectSeatsButton.setDisable(!canBook);
        selectSeatsButton.setText("Select Seats · " + selected.getFlightNumber());
        flightSelectionHintLabel.setText(
                selected.getFlightNumber() + " (" + ReservationGuiController.formatRoute(selected) + ") — "
                        + selected.getAvailableSeats().size() + " open seat(s) · Base fare "
                        + ReservationGuiController.formatCurrency(selected.getBasePrice())
        );
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
            if (matches.isEmpty()) {
                showInlineWarning(
                        "NO FLIGHTS",
                        "No flights found for " + depCode + " -> " + arrCode
                                + " on " + travelDatePicker.getEditor().getText()
                                + ". Click 'All Scheduled' to view all available flights."
                );
            } else {
                hideInlineNotice();
            }
        } catch (AirlineReservationException ex) {
            showInlineError("SEARCH ERROR", ex.getMessage());
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
        refreshTableItems(flightTable, displayedFlights, Flight::getFlightId);
        if (flightTable.getSelectionModel().getSelectedItem() == null && !displayedFlights.isEmpty()) {
            flightTable.getSelectionModel().selectFirst();
        }
        flightCountLabel.setText("Showing " + displayedFlights.size() + " scheduled flight(s)");
    }

    private void openSeatSelectionDialog() {
        Window owner = ownerWindow();
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInlineWarning("SELECT FLIGHT", "Please select a flight from the table first.");
            return;
        }

        try {
            SeatSelectionDialog dialog = new SeatSelectionDialog(owner, controller, selected);
            ArbitrumTheme.showModalWithBackdrop(owner, dialog);
            Booking created = dialog.getCreatedBooking();
            if (created != null) {
                refreshTablesAfterMutation();
                tabs.getSelectionModel().select(myBookingsTab);
                selectBookingById(created.getBookingId());

                if (dialog.isProceedToPaymentRequested()) {
                    openPaymentDialogForBooking(created);
                } else {
                    showInlineSuccess(
                            "BOOKING HELD",
                            "Booking " + created.getBookingId() + " created (" + created.getStatus() + ") for flight "
                                    + selected.getFlightNumber() + " — Seats: "
                                    + ReservationGuiController.formatSeatNumbers(created)
                                    + ". Click 'Pay Booking' whenever you are ready to settle."
                    );
                }
            }
        } catch (AirlineReservationException ex) {
            showInlineError("SEAT MAP ERROR", ex.getMessage());
        }
    }

    private BorderPane createMyBookingsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(4, 0, 0, 0));

        configureBookingTable();
        BorderPane actionBar = createBookingActionBar();
        BorderPane.setMargin(actionBar, new Insets(ArbitrumTheme.SPACE_12, 0, 0, 0));

        layout.setCenter(bookingTable);
        layout.setBottom(actionBar);
        return layout;
    }

    private void configureBookingTable() {
        configureExplorerTable(bookingTable, "You have no bookings yet. Reserve cabin seats in 'Search & Book Flights'.");

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

        bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldBooking, newBooking) -> {
            updateBookingActionState(newBooking);
        });

        bookingTable.setRowFactory(tv -> {
            TableRow<Booking> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Booking clicked = row.getItem();
                    if (clicked.getStatus() == BookingStatus.PENDING) {
                        openPaymentDialog();
                    } else if (clicked.getStatus() == BookingStatus.CONFIRMED) {
                        checkInSelectedBooking();
                    }
                }
            });
            return row;
        });

        updateBookingActionState(null);
    }

    private BorderPane createBookingActionBar() {
        BorderPane bar = new BorderPane();
        bar.getStyleClass().add("action-footer-bar");

        VBox leftMeta = new VBox(2, bookingCountLabel, bookingSelectionHintLabel);
        leftMeta.setAlignment(Pos.CENTER_LEFT);

        payBookingButton.setOnAction(e -> openPaymentDialog());
        checkInButton.setOnAction(e -> checkInSelectedBooking());

        cancelBookingButton.getStyleClass().add("btn-danger-pill");
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());

        HBox actions = new HBox(ArbitrumTheme.SPACE_12, payBookingButton, checkInButton, cancelBookingButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bar.setLeft(leftMeta);
        bar.setRight(actions);
        return bar;
    }

    private void updateBookingActionState(Booking selected) {
        if (selected == null) {
            payBookingButton.setDisable(true);
            checkInButton.setDisable(true);
            cancelBookingButton.setDisable(true);
            bookingSelectionHintLabel.setText("Select a booking row to settle payment, check in, or cancel.");
            return;
        }

        BookingStatus status = selected.getStatus();
        boolean canPay = status == BookingStatus.PENDING;
        boolean canCheckIn = status == BookingStatus.CONFIRMED;
        boolean canCancel = status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;

        payBookingButton.setDisable(!canPay);
        checkInButton.setDisable(!canCheckIn);
        cancelBookingButton.setDisable(!canCancel);

        String nextStep = switch (status) {
            case PENDING -> "Awaiting settlement — click 'Pay Booking' (or double-click row) to confirm seats.";
            case CONFIRMED -> "Payment settled — click 'Check In' (or double-click row) before boarding.";
            case CHECKED_IN -> "Checked in for flight " + selected.getFlight().getFlightNumber() + " — ready to board.";
            case COMPLETED -> "Journey completed on flight " + selected.getFlight().getFlightNumber() + ".";
            case CANCELLED -> "Booking cancelled — seats released back to inventory.";
        };
        bookingSelectionHintLabel.setText(selected.getBookingId() + ": " + nextStep);
    }

    private void refreshBookingTable() {
        if (!controller.isPassengerLoggedIn()) {
            bookingTable.getItems().clear();
            bookingCountLabel.setText("Showing 0 booking(s)");
            updateBookingActionState(null);
            return;
        }
        List<Booking> bookings = controller.getPassengerBookings();
        refreshTableItems(bookingTable, bookings, Booking::getBookingId);
        if (bookingTable.getSelectionModel().getSelectedItem() == null && !bookings.isEmpty()) {
            bookingTable.getSelectionModel().selectFirst();
        }
        bookingCountLabel.setText("Showing " + bookings.size() + " booking(s)");
        updateBookingActionState(bookingTable.getSelectionModel().getSelectedItem());
    }

    private void selectBookingById(String bookingId) {
        for (Booking item : bookingTable.getItems()) {
            if (item.getBookingId().equals(bookingId)) {
                bookingTable.getSelectionModel().select(item);
                bookingTable.scrollTo(item);
                break;
            }
        }
    }

    private void refreshTablesAfterMutation() {
        flightTable.refresh();
        populateFlightTable(displayedFlights);
        refreshBookingTable();
    }

    private Booking getSelectedBooking() {
        return bookingTable.getSelectionModel().getSelectedItem();
    }

    private void openPaymentDialog() {
        Booking selected = getSelectedBooking();
        if (selected == null) {
            showInlineWarning("SELECT BOOKING", "Please select a pending booking from the table first.");
            return;
        }
        openPaymentDialogForBooking(selected);
    }

    private void openPaymentDialogForBooking(Booking targetBooking) {
        Window owner = ownerWindow();
        try {
            Booking payable = controller.validateBookingPayable(targetBooking.getBookingId());
            PaymentDialog dialog = new PaymentDialog(owner, controller, payable, this::refreshTablesAfterMutation);
            ArbitrumTheme.showModalWithBackdrop(owner, dialog);
            refreshTablesAfterMutation();
            selectBookingById(targetBooking.getBookingId());

            if (payable.getStatus() == BookingStatus.CONFIRMED) {
                showInlineSuccess(
                        "PAYMENT CONFIRMED",
                        "Booking " + payable.getBookingId() + " is now CONFIRMED ("
                                + ReservationGuiController.formatCurrency(payable.getTotalPrice())
                                + "). You can now Check In."
                );
            } else if (controller.getBookingPaymentStatus(payable) == PaymentStatus.FAILED) {
                showInlineWarning(
                        "PAYMENT FAILED",
                        "Last payment attempt for " + payable.getBookingId()
                                + " failed. Seats remain held in PENDING — click 'Pay Booking' to retry."
                );
            }
        } catch (AirlineReservationException ex) {
            showInlineError("PAYMENT UNAVAILABLE", ex.getMessage());
        }
    }

    private void checkInSelectedBooking() {
        Booking selected = getSelectedBooking();
        if (selected == null) {
            showInlineWarning("SELECT BOOKING", "Please select a confirmed booking to check in.");
            return;
        }

        try {
            Booking updated = controller.checkInBooking(selected.getBookingId());
            refreshTablesAfterMutation();
            selectBookingById(updated.getBookingId());
            showInlineSuccess(
                    "CHECKED IN",
                    "Boarding pass ready! Booking " + updated.getBookingId() + " is now " + updated.getStatus()
                            + " on flight " + updated.getFlight().getFlightNumber()
                            + " (Seats: " + ReservationGuiController.formatSeatNumbers(updated) + ")."
            );
        } catch (AirlineReservationException ex) {
            showInlineError("CHECK-IN FAILED", ex.getMessage());
        }
    }

    private void cancelSelectedBooking() {
        Window owner = ownerWindow();
        Booking selected = getSelectedBooking();
        if (selected == null) {
            showInlineWarning("SELECT BOOKING", "Please select a booking to cancel.");
            return;
        }

        if (selected.getStatus() != BookingStatus.PENDING && selected.getStatus() != BookingStatus.CONFIRMED) {
            showInlineError(
                    "CANNOT CANCEL",
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
            selectBookingById(selected.getBookingId());
            showInlineSuccess(
                    "BOOKING CANCELLED",
                    "Booking " + selected.getBookingId() + " has been CANCELLED and its seats are now AVAILABLE."
                            + (hasCompletedPayment ? " Payment marked REFUNDED." : "")
            );
        } catch (AirlineReservationException ex) {
            showInlineError("CANCELLATION FAILED", ex.getMessage());
        }
    }

    private static VBox labeledInput(String labelText, Node input) {
        return new VBox(ArbitrumTheme.SPACE_8, ArbitrumTheme.createFieldLabel(labelText), input);
    }
}
