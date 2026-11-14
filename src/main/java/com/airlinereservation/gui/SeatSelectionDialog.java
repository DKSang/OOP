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
 * Modal JavaFX dialog rendering a flight's cabin seat map in a responsive Web3 grid with live fare preview.
 */
public class SeatSelectionDialog extends Stage {

    private static final int CABIN_COLUMNS = 4;

    private final ReservationGuiController controller;
    private final Flight flight;
    private final Set<String> selectedSeatNumbers = new LinkedHashSet<>();

    private final Label selectedSeatsLabel = ArbitrumTheme.createMonoLabel("");
    private final Label totalPriceLabel = ArbitrumTheme.createHeadingLabel("", "heading-md");
    private Booking createdBooking;

    public SeatSelectionDialog(Window owner, ReservationGuiController controller, Flight flight) {
        this.controller = controller;
        this.flight = flight;

        setTitle("Select Seats - " + flight.getFlightNumber());
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setMinWidth(820);
        setMinHeight(680);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("bg-repeating-lines");
        root.setPadding(new Insets(ArbitrumTheme.SPACE_24));

        // Hero header + legend
        Label eyebrow = ArbitrumTheme.createCyanEyebrowLabel("CABIN ALLOCATION // SEAT MAP");
        Label heading = ArbitrumTheme.createDisplayTitle("Cabin Seat Map - " + flight.getFlightNumber(), "display-md");
        Label routeInfo = ArbitrumTheme.createHeroSubtitle(
                flight.getDepartureAirport().getAirportCode()
                        + " -> " + flight.getArrivalAirport().getAirportCode()
                        + "  |  Aircraft: " + flight.getAircraft().getModel()
                        + "  |  Base Fare: " + ReservationGuiController.formatCurrency(flight.getBasePrice())
        );

        VBox heroBanner = new VBox(ArbitrumTheme.SPACE_8, eyebrow, heading, routeInfo);
        heroBanner.getStyleClass().add("hero-banner");

        VBox headerPanel = new VBox(ArbitrumTheme.SPACE_12, heroBanner, buildLegendRow());
        BorderPane.setMargin(headerPanel, new Insets(0, 0, ArbitrumTheme.SPACE_16, 0));

        // Seat grid
        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(ArbitrumTheme.SPACE_12);
        seatGrid.setVgap(ArbitrumTheme.SPACE_12);
        seatGrid.setPadding(new Insets(ArbitrumTheme.SPACE_16));

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
        seatScrollPane.setPrefViewportHeight(350);

        // Footer summary & actions (yellow-to-cyan accent over stone-white surface)
        Label summaryEyebrow = ArbitrumTheme.createEyebrowLabel("LIVE FARE PREVIEW");
        VBox summaryPanel = new VBox(4, summaryEyebrow, selectedSeatsLabel, totalPriceLabel);
        summaryPanel.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = ArbitrumTheme.createSecondaryButton("Cancel");
        cancelButton.setOnAction(e -> close());

        Button confirmButton = ArbitrumTheme.createPrimaryButton("Confirm Booking");
        confirmButton.setOnAction(e -> confirmBooking());

        HBox actionButtons = new HBox(ArbitrumTheme.SPACE_12, cancelButton, confirmButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        BorderPane footerPanel = new BorderPane();
        footerPanel.getStyleClass().add("fare-summary-bar");
        footerPanel.setLeft(summaryPanel);
        footerPanel.setRight(actionButtons);
        BorderPane.setMargin(footerPanel, new Insets(ArbitrumTheme.SPACE_16, 0, 0, 0));

        root.setTop(headerPanel);
        root.setCenter(seatScrollPane);
        root.setBottom(footerPanel);

        updateSelectionSummary();

        Scene scene = new Scene(root, 880, 720);
        ArbitrumTheme.applyStylesheet(scene);
        setScene(scene);
    }

    public Booking getCreatedBooking() {
        return createdBooking;
    }

    private FlowPane buildLegendRow() {
        FlowPane legend = new FlowPane(ArbitrumTheme.SPACE_8, ArbitrumTheme.SPACE_8);
        legend.getChildren().addAll(
                ArbitrumTheme.createBadgeLabel("Business (Lounge)", "badge-business"),
                ArbitrumTheme.createBadgeLabel("Economy+ Legroom", "badge-legroom"),
                ArbitrumTheme.createBadgeLabel("Economy", "badge-economy"),
                ArbitrumTheme.createBadgeLabel("Selected", "badge-selected"),
                ArbitrumTheme.createBadgeLabel("Reserved", "badge-reserved"),
                ArbitrumTheme.createBadgeLabel("Occupied", "badge-occupied")
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
        button.setPrefHeight(86);
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
            ArbitrumTheme.showInfo(
                    this,
                    "Booking Hold Confirmed",
                    "Booking " + booking.getBookingId() + " created (" + booking.getStatus() + ").\n"
                            + "Flight: " + flight.getFlightNumber() + "\n"
                            + "Seats: " + String.join(", ", seatsToBook) + "\n"
                            + "Total Fare: " + ReservationGuiController.formatCurrency(booking.getTotalPrice())
            );
            close();
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(this, "Seat Booking Failed", ex.getMessage());
        }
    }

    private record SeatPresentation(String tierLine, String statusOrPrice, String tooltip, String tileStyleClass) {
    }
}
