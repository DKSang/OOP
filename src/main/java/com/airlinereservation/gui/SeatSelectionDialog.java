package com.airlinereservation.gui;

import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.BusinessSeat;
import com.airlinereservation.model.EconomySeat;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Seat;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Modal JavaFX dialog rendering a flight's cabin seat map as a 2-aisle-2 aircraft fuselage
 * with cabin class zone dividers, live fare preview, and direct Book-to-Pay continuation.
 */
public class SeatSelectionDialog extends Stage {

    private static final int SEATS_PER_ROW = 4;
    private static final String[] COLUMN_HEADERS = {
            "A · WINDOW", "B · AISLE", "AISLE", "C · AISLE", "D · WINDOW"
    };

    private final ReservationGuiController controller;
    private final Flight flight;
    private final Set<String> selectedSeatNumbers = new LinkedHashSet<>();

    private final Label selectedSeatsLabel = ArbitrumTheme.createMonoLabel("");
    private final Label totalPriceLabel = ArbitrumTheme.createHeadingLabel("", "heading-md");
    private final Button holdBookingButton = ArbitrumTheme.createSecondaryButton("Hold Seats (Pay Later)");
    private final Button bookAndPayButton = ArbitrumTheme.createPrimaryButton("Reserve & Pay Now");

    private Booking createdBooking;
    private boolean proceedToPaymentRequested;

    public SeatSelectionDialog(Window owner, ReservationGuiController controller, Flight flight) {
        this.controller = controller;
        this.flight = flight;

        setTitle("Select Cabin Seats — " + flight.getFlightNumber());
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setMinWidth(840);
        setMinHeight(700);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("bg-repeating-lines");
        root.setPadding(new Insets(ArbitrumTheme.SPACE_24));

        // Compact header + legend
        Label eyebrow = ArbitrumTheme.createCyanEyebrowLabel("CABIN SEAT ALLOCATION");
        Label heading = ArbitrumTheme.createDisplayTitle(
                flight.getFlightNumber() + " · " + ReservationGuiController.formatRoute(flight),
                "display-md"
        );
        Label routeInfo = ArbitrumTheme.createHeroSubtitle(
                "Aircraft: " + flight.getAircraft().getModel()
                        + "  ·  Departure: " + ReservationGuiController.formatDateTime(flight.getDepartureTime())
                        + "  ·  Base Fare: " + ReservationGuiController.formatCurrency(flight.getBasePrice())
        );

        VBox heroBanner = new VBox(4, eyebrow, heading, routeInfo);
        heroBanner.getStyleClass().add("workspace-banner");

        VBox headerPanel = new VBox(ArbitrumTheme.SPACE_12, heroBanner, buildLegendRow());
        BorderPane.setMargin(headerPanel, new Insets(0, 0, ArbitrumTheme.SPACE_12, 0));

        // Aircraft fuselage container inside scroll pane
        VBox fuselage = buildAircraftFuselage(controller.getSeatsForFlight(flight.getFlightId()));
        StackWrapper fuselageWrapper = new StackWrapper(fuselage);

        ScrollPane seatScrollPane = new ScrollPane(fuselageWrapper);
        seatScrollPane.getStyleClass().add("seat-scroll");
        seatScrollPane.setFitToWidth(true);
        seatScrollPane.setPrefViewportHeight(400);

        // Footer summary & actions
        VBox summaryPanel = new VBox(2, selectedSeatsLabel, totalPriceLabel);
        summaryPanel.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = ArbitrumTheme.createSecondaryButton("Cancel");
        cancelButton.setOnAction(e -> close());

        holdBookingButton.setOnAction(e -> confirmBooking(false));
        bookAndPayButton.setOnAction(e -> confirmBooking(true));

        HBox actionButtons = new HBox(ArbitrumTheme.SPACE_12, cancelButton, holdBookingButton, bookAndPayButton);
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

        Scene scene = new Scene(root, 900, 740);
        ArbitrumTheme.applyStylesheet(scene);
        setScene(scene);
    }

    public Booking getCreatedBooking() {
        return createdBooking;
    }

    public boolean isProceedToPaymentRequested() {
        return proceedToPaymentRequested;
    }

    private FlowPane buildLegendRow() {
        FlowPane legend = new FlowPane(ArbitrumTheme.SPACE_8, ArbitrumTheme.SPACE_8);
        legend.getChildren().addAll(
                ArbitrumTheme.createBadgeLabel("Business (2.0x · Lounge)", "badge-business"),
                ArbitrumTheme.createBadgeLabel("Economy+ (1.15x · Legroom)", "badge-legroom"),
                ArbitrumTheme.createBadgeLabel("Economy (1.0x)", "badge-economy"),
                ArbitrumTheme.createBadgeLabel("Selected", "badge-selected"),
                ArbitrumTheme.createBadgeLabel("Reserved (Hold)", "badge-reserved"),
                ArbitrumTheme.createBadgeLabel("Occupied", "badge-occupied")
        );
        return legend;
    }

    private VBox buildAircraftFuselage(List<Seat> seats) {
        VBox fuselage = new VBox(ArbitrumTheme.SPACE_12);
        fuselage.getStyleClass().add("cabin-fuselage");
        fuselage.setMaxWidth(760);

        // Cockpit / Nose cone indicator
        Label noseLabel = ArbitrumTheme.createEyebrowLabel("▲  FRONT OF AIRCRAFT · FLIGHT DECK  ▲");
        HBox noseBanner = new HBox(noseLabel);
        noseBanner.setAlignment(Pos.CENTER);
        noseBanner.getStyleClass().add("cabin-nose-banner");

        GridPane cabinGrid = new GridPane();
        cabinGrid.setHgap(ArbitrumTheme.SPACE_12);
        cabinGrid.setVgap(10);

        // 5 columns: Seat A (22%), Seat B (22%), Center Aisle (12%), Seat C (22%), Seat D (22%)
        double[] colPercents = {22.0, 22.0, 12.0, 22.0, 22.0};
        for (int c = 0; c < colPercents.length; c++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(colPercents[c]);
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(HPos.CENTER);
            cabinGrid.getColumnConstraints().add(col);
        }

        // Column header row
        for (int c = 0; c < COLUMN_HEADERS.length; c++) {
            Label colHeader = new Label(COLUMN_HEADERS[c]);
            colHeader.getStyleClass().add("cabin-col-header");
            cabinGrid.add(colHeader, c, 0);
        }

        int gridRow = 1;
        String currentZone = null;
        int totalRows = (int) Math.ceil((double) seats.size() / SEATS_PER_ROW);

        for (int cabinRowIndex = 0; cabinRowIndex < totalRows; cabinRowIndex++) {
            int rowStart = cabinRowIndex * SEATS_PER_ROW;
            Seat firstSeatInRow = seats.get(rowStart);
            String rowZone = classifyCabinZone(firstSeatInRow);

            if (!rowZone.equals(currentZone)) {
                currentZone = rowZone;
                Node zoneDivider = createZoneDivider(rowZone);
                cabinGrid.add(zoneDivider, 0, gridRow, 5, 1);
                gridRow++;
            }

            int displayRowNumber = cabinRowIndex + 1;
            Label aisleMarker = new Label(String.format(Locale.ROOT, "%02d", displayRowNumber));
            aisleMarker.getStyleClass().add("cabin-aisle-marker");
            cabinGrid.add(aisleMarker, 2, gridRow);

            for (int seatCol = 0; seatCol < SEATS_PER_ROW; seatCol++) {
                int seatIndex = rowStart + seatCol;
                if (seatIndex >= seats.size()) {
                    break;
                }
                int targetGridCol = seatCol < 2 ? seatCol : seatCol + 1;
                cabinGrid.add(createSeatToggleButton(seats.get(seatIndex)), targetGridCol, gridRow);
            }
            gridRow++;
        }

        fuselage.getChildren().addAll(noseBanner, cabinGrid);
        return fuselage;
    }

    private static String classifyCabinZone(Seat seat) {
        if (seat instanceof BusinessSeat) {
            return "BUSINESS CABIN · PRIORITY & LOUNGE ACCESS";
        }
        if (seat instanceof EconomySeat economySeat && economySeat.hasExtraLegroom()) {
            return "ECONOMY+ · EXTRA LEGROOM ROW";
        }
        return "MAIN CABIN · STANDARD ECONOMY";
    }

    private static Node createZoneDivider(String zoneTitle) {
        Label label = ArbitrumTheme.createEyebrowLabel(zoneTitle);
        Region leftLine = new Region();
        Region rightLine = new Region();
        HBox.setHgrow(leftLine, Priority.ALWAYS);
        HBox.setHgrow(rightLine, Priority.ALWAYS);

        HBox box = new HBox(ArbitrumTheme.SPACE_12, leftLine, label, rightLine);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("cabin-zone-divider");
        return box;
    }

    private ToggleButton createSeatToggleButton(Seat seat) {
        SeatPresentation presentation = describeSeat(seat);

        Label codeLabel = new Label(seat.getSeatNumber());
        codeLabel.getStyleClass().add("seat-code");

        Label metaLabel = new Label(presentation.tierLine());
        metaLabel.getStyleClass().add("seat-meta");

        Label priceLabel = new Label(presentation.statusOrPrice());
        priceLabel.getStyleClass().add("seat-price");

        VBox content = new VBox(2, codeLabel, metaLabel, priceLabel);
        content.setAlignment(Pos.CENTER);

        ToggleButton button = new ToggleButton();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(78);
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
            tierLine = "Business" + loungeTag;
            tooltipDetail = "Multiplier: " + businessSeat.getPriceMultiplier()
                    + "x | Lounge Access: " + (businessSeat.hasLoungeAccess() ? "Yes" : "No");
            tileStyleClass = seat.isAvailable() ? "seat-business" : unavailableClass(seat);
        } else if (seat instanceof EconomySeat economySeat && economySeat.hasExtraLegroom()) {
            tierLine = "Extra Legroom";
            tooltipDetail = "Economy+ (1.15x)";
            tileStyleClass = seat.isAvailable() ? "seat-legroom" : unavailableClass(seat);
        } else {
            tierLine = "Economy";
            tooltipDetail = "Standard Cabin (1.0x)";
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
        boolean empty = selectedSeatNumbers.isEmpty();
        holdBookingButton.setDisable(empty);
        bookAndPayButton.setDisable(empty);

        if (empty) {
            selectedSeatsLabel.setText("Click available cabin seats above to select (0 selected)");
            totalPriceLabel.setText("Total Fare: " + ReservationGuiController.formatCurrency(0.0));
            return;
        }

        List<String> seatsList = List.copyOf(selectedSeatNumbers);
        double previewTotal = controller.previewSeatSelectionPrice(flight.getFlightId(), seatsList);
        selectedSeatsLabel.setText(
                seatsList.size() + " Seat(s) Selected: " + String.join(", ", seatsList)
        );
        totalPriceLabel.setText("Total Fare: " + ReservationGuiController.formatCurrency(previewTotal));
    }

    private void confirmBooking(boolean proceedToPayment) {
        List<String> seatsToBook = List.copyOf(selectedSeatNumbers);
        try {
            Booking booking = controller.bookSeats(flight.getFlightId(), seatsToBook);
            this.createdBooking = booking;
            this.proceedToPaymentRequested = proceedToPayment;
            close();
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(this, "Seat Booking Failed", ex.getMessage());
        }
    }

    private static final class StackWrapper extends HBox {
        StackWrapper(Node child) {
            super(child);
            setAlignment(Pos.TOP_CENTER);
            setPadding(new Insets(ArbitrumTheme.SPACE_16));
            HBox.setHgrow(child, Priority.ALWAYS);
        }
    }

    private record SeatPresentation(String tierLine, String statusOrPrice, String tooltip, String tileStyleClass) {
    }
}
