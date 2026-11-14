package com.airlinereservation.gui;

import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.BusinessSeat;
import com.airlinereservation.model.EconomySeat;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Seat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Modal JavaFX dialog rendering a flight's cabin seat map in a responsive grid with live fare preview.
 */
public class SeatSelectionDialog extends Stage {

    private static final int CABIN_COLUMNS = 4;

    private final ReservationGuiController controller;
    private final Flight flight;
    private final Set<String> selectedSeatNumbers = new LinkedHashSet<>();

    private final Label selectedSeatsLabel = AnthropicTheme.createSectionLabel("");
    private final Label totalPriceLabel = AnthropicTheme.createHeadingLabel("", "heading-md");
    private Booking createdBooking;

    public SeatSelectionDialog(Window owner, ReservationGuiController controller, Flight flight) {
        this.controller = controller;
        this.flight = flight;

        setTitle("Select Seats - " + flight.getFlightNumber());
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setMinWidth(780);
        setMinHeight(640);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(AnthropicTheme.SPACE_24));

        // Header + legend
        Label heading = AnthropicTheme.createHeadingLabel("Cabin Seat Map - " + flight.getFlightNumber(), "heading-lg");
        Label routeInfo = AnthropicTheme.createMutedLabel(
                flight.getDepartureAirport().getAirportCode()
                        + " -> " + flight.getArrivalAirport().getAirportCode()
                        + "  |  Aircraft: " + flight.getAircraft().getModel()
                        + "  |  Base Fare: " + ReservationGuiController.formatCurrency(flight.getBasePrice())
        );

        VBox headerPanel = new VBox(AnthropicTheme.SPACE_12, new VBox(AnthropicTheme.SPACE_8, heading, routeInfo), buildLegendRow());
        BorderPane.setMargin(headerPanel, new Insets(0, 0, AnthropicTheme.SPACE_16, 0));

        // Seat grid
        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(AnthropicTheme.SPACE_12);
        seatGrid.setVgap(AnthropicTheme.SPACE_12);
        seatGrid.setPadding(new Insets(AnthropicTheme.SPACE_16));

        for (int c = 0; c < CABIN_COLUMNS; c++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / CABIN_COLUMNS);
            col.setHgrow(Priority.ALWAYS);
            seatGrid.getColumnConstraints().add(col);
        }

        List<Seat> seats = controller.getSeatsForFlight(flight.getFlightId());
        for (int i = 0; i < seats.size(); i++) {
            seatGrid.add(createSeatToggleButton(seats.get(i)), i % CABIN_COLUMNS, i / CABIN_COLUMNS);
        }

        ScrollPane seatScrollPane = new ScrollPane(seatGrid);
        seatScrollPane.getStyleClass().add("seat-scroll");
        seatScrollPane.setFitToWidth(true);
        seatScrollPane.setPrefViewportHeight(360);

        // Footer summary & actions
        VBox summaryPanel = new VBox(AnthropicTheme.SPACE_8, selectedSeatsLabel, totalPriceLabel);
        summaryPanel.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = AnthropicTheme.createSecondaryButton("Cancel");
        cancelButton.setOnAction(e -> close());

        Button confirmButton = AnthropicTheme.createPrimaryButton("Confirm Booking");
        confirmButton.setOnAction(e -> confirmBooking());

        HBox actionButtons = new HBox(AnthropicTheme.SPACE_12, cancelButton, confirmButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        BorderPane footerPanel = new BorderPane();
        footerPanel.getStyleClass().add("subtle-panel");
        footerPanel.setLeft(summaryPanel);
        footerPanel.setRight(actionButtons);
        BorderPane.setMargin(footerPanel, new Insets(AnthropicTheme.SPACE_16, 0, 0, 0));

        root.setTop(headerPanel);
        root.setCenter(seatScrollPane);
        root.setBottom(footerPanel);

        updateSelectionSummary();

        Scene scene = new Scene(root, 840, 680);
        AnthropicTheme.applyStylesheet(scene);
        setScene(scene);
    }

    public Booking getCreatedBooking() {
        return createdBooking;
    }

    private FlowPane buildLegendRow() {
        FlowPane legend = new FlowPane(AnthropicTheme.SPACE_8, AnthropicTheme.SPACE_8);
        legend.getChildren().addAll(
                AnthropicTheme.createBadgeLabel("Business (Lounge)", "badge-business"),
                AnthropicTheme.createBadgeLabel("Economy+ Legroom", "badge-legroom"),
                AnthropicTheme.createBadgeLabel("Economy", "badge-economy"),
                AnthropicTheme.createBadgeLabel("Selected", "badge-selected"),
                AnthropicTheme.createBadgeLabel("Reserved", "badge-reserved"),
                AnthropicTheme.createBadgeLabel("Occupied", "badge-occupied")
        );
        return legend;
    }

    private ToggleButton createSeatToggleButton(Seat seat) {
        SeatPresentation presentation = describeSeat(seat);

        Label codeLabel = new Label(seat.getSeatNumber());
        codeLabel.getStyleClass().add("seat-code");

        Label metaLabel = new Label(presentation.tierLine());
        metaLabel.getStyleClass().add("seat-meta");

        Label priceLabel = new Label(presentation.statusOrPrice());
        priceLabel.getStyleClass().add("seat-price");

        VBox content = new VBox(4, codeLabel, metaLabel, priceLabel);
        content.setAlignment(Pos.CENTER);

        ToggleButton button = new ToggleButton();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(84);
        button.getStyleClass().addAll("seat-tile", presentation.tileStyleClass());
        button.setTooltip(new Tooltip(presentation.tooltip()));

        if (!seat.isAvailable()) {
            button.setDisable(true);
            return button;
        }

        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                selectedSeatNumbers.add(seat.getSeatNumber());
            } else {
                selectedSeatNumbers.remove(seat.getSeatNumber());
            }
            updateSelectionSummary();
        });
        return button;
    }

    private SeatPresentation describeSeat(Seat seat) {
        double seatPrice = controller.previewSeatSelectionPrice(flight.getFlightId(), List.of(seat.getSeatNumber()));
        String priceText = ReservationGuiController.formatCurrency(seatPrice);

        String tierLine;
        String tooltipDetail;
        String tileStyleClass;

        if (seat instanceof BusinessSeat businessSeat) {
            String loungeTag = businessSeat.hasLoungeAccess() ? " · Lounge" : "";
            tierLine = "Business x" + businessSeat.getPriceMultiplier() + loungeTag;
            tooltipDetail = "Multiplier: " + businessSeat.getPriceMultiplier()
                    + "x | Lounge Access: " + (businessSeat.hasLoungeAccess() ? "Yes" : "No");
            tileStyleClass = seat.isAvailable() ? "seat-business" : unavailableClass(seat);
        } else if (seat instanceof EconomySeat economySeat && economySeat.hasExtraLegroom()) {
            tierLine = "Economy+ Legroom";
            tooltipDetail = "Extra Legroom";
            tileStyleClass = seat.isAvailable() ? "seat-legroom" : unavailableClass(seat);
        } else {
            tierLine = "Economy";
            tooltipDetail = "Standard Cabin";
            tileStyleClass = seat.isAvailable() ? "seat-economy" : unavailableClass(seat);
        }

        String statusOrPrice = seat.isAvailable() ? priceText : seat.getStatus().name();
        String tooltip = seat.getSeatNumber() + " (" + seat.getSeatType() + ", " + seat.getStatus() + ") - "
                + priceText + " | " + tooltipDetail;

        return new SeatPresentation(tierLine, statusOrPrice, tooltip, tileStyleClass);
    }

    private static String unavailableClass(Seat seat) {
        return seat.getStatus() == SeatStatus.RESERVED ? "seat-reserved" : "seat-occupied";
    }

    private void updateSelectionSummary() {
        if (selectedSeatNumbers.isEmpty()) {
            selectedSeatsLabel.setText("Selected Seats: None");
            totalPriceLabel.setText("Total Price: " + ReservationGuiController.formatCurrency(0.0));
            return;
        }

        List<String> seatsList = List.copyOf(selectedSeatNumbers);
        double previewTotal = controller.previewSeatSelectionPrice(flight.getFlightId(), seatsList);
        selectedSeatsLabel.setText("Selected Seats: " + String.join(", ", seatsList));
        totalPriceLabel.setText("Total Price: " + ReservationGuiController.formatCurrency(previewTotal));
    }

    private void confirmBooking() {
        List<String> seatsToBook = List.copyOf(selectedSeatNumbers);
        try {
            Booking booking = controller.bookSeats(flight.getFlightId(), seatsToBook);
            this.createdBooking = booking;
            AnthropicTheme.showInfo(
                    this,
                    "Booking Hold Confirmed",
                    "Booking " + booking.getBookingId() + " created (" + booking.getStatus() + ").\n"
                            + "Flight: " + flight.getFlightNumber() + "\n"
                            + "Seats: " + String.join(", ", seatsToBook) + "\n"
                            + "Total Fare: " + ReservationGuiController.formatCurrency(booking.getTotalPrice())
            );
            close();
        } catch (AirlineReservationException ex) {
            AnthropicTheme.showError(this, "Seat Booking Failed", ex.getMessage());
        }
    }

    private record SeatPresentation(String tierLine, String statusOrPrice, String tooltip, String tileStyleClass) {
    }
}
