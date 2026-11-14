package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Person;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
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
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * Role workspace container for authenticated {@code Passenger} sessions.
 */
public class PassengerWorkspaceView extends AbstractWorkspaceView {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ComboBox<Airport> departureCombo = new ComboBox<>();
    private final ComboBox<Airport> arrivalCombo = new ComboBox<>();
    private final DatePicker travelDatePicker;
    private final TextField seatCountField = new TextField("1");
    private final Label flightCountLabel = AnthropicTheme.createMutedLabel("Showing 0 flight(s)");
    private final TableView<Flight> flightTable = new TableView<>();

    private List<Flight> displayedFlights = List.of();

    public PassengerWorkspaceView(ReservationGuiController controller) {
        super(
                controller,
                "Passenger Workspace",
                "Flight search, interactive seat selection, and booking payment tabs connect here."
        );
        this.travelDatePicker = AnthropicTheme.createIsoDatePicker(controller.getDefaultTravelDate());

        tabs.getTabs().addAll(
                createNonClosableTab("Search & Book Flights", createSearchFlightsTab()),
                createPlaceholderTab(
                        "My Bookings",
                        "My Bookings",
                        "Passenger booking history, payment retry, check-in, and cancellation will appear here."
                )
        );
    }

    @Override
    public void refreshData() {
        super.refreshData();
        populateAirportSelectors();
        resetFiltersAndShowAllScheduled();
    }

    @Override
    protected String formatWelcomeTitle(Person user) {
        return "Welcome, " + user.getFullName() + " (" + user.getId() + ")";
    }

    private BorderPane createSearchFlightsTab() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(AnthropicTheme.SPACE_8, 0, 0, 0));

        VBox filterCard = createSearchFilterCard();
        BorderPane.setMargin(filterCard, new Insets(0, 0, AnthropicTheme.SPACE_16, 0));

        configureFlightTable();
        BorderPane bottomBar = createBottomActionBar();
        BorderPane.setMargin(bottomBar, new Insets(AnthropicTheme.SPACE_16, 0, 0, 0));

        layout.setTop(filterCard);
        layout.setCenter(flightTable);
        layout.setBottom(bottomBar);
        return layout;
    }

    private VBox createSearchFilterCard() {
        VBox card = new VBox(AnthropicTheme.SPACE_16);
        card.getStyleClass().add("card");

        Label sectionTitle = AnthropicTheme.createSectionLabel("Find Available Flights");

        GridPane fieldsGrid = new GridPane();
        fieldsGrid.setHgap(AnthropicTheme.SPACE_16);
        fieldsGrid.setVgap(AnthropicTheme.SPACE_8);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            fieldsGrid.getColumnConstraints().add(col);
        }

        configureAirportCombo(departureCombo);
        configureAirportCombo(arrivalCombo);
        AnthropicTheme.styleTextInput(seatCountField);
        seatCountField.setOnAction(e -> executeFlightSearch());

        fieldsGrid.add(AnthropicTheme.createFieldLabel("Departure Airport"), 0, 0);
        fieldsGrid.add(AnthropicTheme.createFieldLabel("Arrival Airport"), 1, 0);
        fieldsGrid.add(AnthropicTheme.createFieldLabel("Travel Date (YYYY-MM-DD)"), 2, 0);
        fieldsGrid.add(AnthropicTheme.createFieldLabel("Seats Needed"), 3, 0);

        fieldsGrid.add(departureCombo, 0, 1);
        fieldsGrid.add(arrivalCombo, 1, 1);
        fieldsGrid.add(travelDatePicker, 2, 1);
        fieldsGrid.add(seatCountField, 3, 1);

        Button searchButton = AnthropicTheme.createPrimaryButton("Search Flights");
        searchButton.setOnAction(e -> executeFlightSearch());

        Button resetButton = AnthropicTheme.createSecondaryButton("Show All Scheduled Flights");
        resetButton.setOnAction(e -> resetFiltersAndShowAllScheduled());

        HBox actionsRow = new HBox(AnthropicTheme.SPACE_12, searchButton, resetButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(sectionTitle, fieldsGrid, actionsRow);
        return card;
    }

    private void configureFlightTable() {
        flightTable.getStyleClass().add("flight-table");
        flightTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        flightTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        flightTable.setPlaceholder(AnthropicTheme.createMutedLabel("No matching flights found."));

        String[] cols = ReservationGuiController.FLIGHT_TABLE_COLUMNS;
        flightTable.getColumns().setAll(List.of(
                textColumn(cols[0], Flight::getFlightId, 70),
                textColumn(cols[1], Flight::getFlightNumber, 80),
                textColumn(cols[2], f -> f.getDepartureAirport().getAirportCode()
                        + " -> " + f.getArrivalAirport().getAirportCode(), 105),
                textColumn(cols[3], f -> DATE_TIME_FORMATTER.format(f.getDepartureTime()), 135),
                textColumn(cols[4], f -> DATE_TIME_FORMATTER.format(f.getArrivalTime()), 135),
                textColumn(cols[5], f -> f.getAircraft().getModel(), 135),
                textColumn(cols[6], f -> String.valueOf(f.getAvailableSeats().size()), 110),
                textColumn(cols[7], f -> ReservationGuiController.formatCurrency(f.getBasePrice()), 125),
                textColumn(cols[8], f -> f.getStatus().name(), 100)
        ));

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

        Button selectSeatsButton = AnthropicTheme.createPrimaryButton("Select Seats & Book");
        selectSeatsButton.setOnAction(e -> openSeatSelectionDialog());

        Label tipLabel = AnthropicTheme.createMutedLabel("Tip: Double-click any flight row or select and press the button.");
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
        resetAirportSelections();
    }

    private void resetAirportSelections() {
        int count = departureCombo.getItems().size();
        if (count >= 2) {
            departureCombo.getSelectionModel().select(0);
            arrivalCombo.getSelectionModel().select(1);
        } else if (count == 1) {
            departureCombo.getSelectionModel().select(0);
            arrivalCombo.getSelectionModel().select(0);
        }
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
            AnthropicTheme.showError(
                    getScene() == null ? null : getScene().getWindow(),
                    "Flight Search Error",
                    ex.getMessage()
            );
        }
    }

    private void resetFiltersAndShowAllScheduled() {
        resetAirportSelections();
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
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AnthropicTheme.showWarning(
                    getScene() == null ? null : getScene().getWindow(),
                    "No Flight Selected",
                    "Please select a flight from the table first."
            );
            return;
        }

        try {
            SeatSelectionDialog dialog = new SeatSelectionDialog(
                    getScene() == null ? null : getScene().getWindow(),
                    controller,
                    selected
            );
            dialog.showAndWait();
            if (dialog.getCreatedBooking() != null) {
                flightTable.refresh();
                populateFlightTable(displayedFlights);
            }
        } catch (AirlineReservationException ex) {
            AnthropicTheme.showError(
                    getScene() == null ? null : getScene().getWindow(),
                    "Unable to Open Seat Map",
                    ex.getMessage()
            );
        }
    }

    private static TableColumn<Flight, String> textColumn(String title,
                                                          Function<Flight, String> extractor,
                                                          double minWidth) {
        TableColumn<Flight, String> col = new TableColumn<>(title);
        col.setMinWidth(minWidth);
        col.setCellValueFactory(cell -> new ReadOnlyStringWrapper(extractor.apply(cell.getValue())));
        return col;
    }

    private static void configureAirportCombo(ComboBox<Airport> comboBox) {
        AnthropicTheme.styleComboBox(comboBox);
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Airport airport) {
                return airport == null ? "" : airport.getAirportCode() + " - " + airport.getCity();
            }

            @Override
            public Airport fromString(String string) {
                return null;
            }
        });
    }
}
