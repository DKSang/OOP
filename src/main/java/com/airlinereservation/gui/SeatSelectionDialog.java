package com.airlinereservation.gui;

import com.airlinereservation.enums.SeatStatus;
import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.BusinessSeat;
import com.airlinereservation.model.EconomySeat;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Seat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Modal dialog rendering a flight's cabin seat map in a grid with live fare preview.
 */
public class SeatSelectionDialog extends JDialog {

    private static final int CABIN_COLUMNS = 4;

    private final ReservationGuiController controller;
    private final Flight flight;
    private final Set<String> selectedSeatNumbers = new LinkedHashSet<>();

    private final JLabel selectedSeatsLabel = AnthropicTheme.createSectionLabel("");
    private final JLabel totalPriceLabel = AnthropicTheme.createHeadingLabel("", 18);
    private Booking createdBooking;

    public SeatSelectionDialog(Window owner, ReservationGuiController controller, Flight flight) {
        super(owner, "Select Seats - " + flight.getFlightNumber(), ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.flight = flight;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AnthropicTheme.PRIMARY_SURFACE);
        setLayout(new BorderLayout(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_16));

        JPanel root = new JPanel(new BorderLayout(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_16));
        root.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        root.setBorder(AnthropicTheme.spacing(AnthropicTheme.SPACE_24, AnthropicTheme.SPACE_24));

        // Header + legend
        JPanel headerPanel = new JPanel(new BorderLayout(0, AnthropicTheme.SPACE_12));
        headerPanel.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        JPanel titleBlock = new JPanel(new GridLayout(0, 1, 0, AnthropicTheme.SPACE_8));
        titleBlock.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        JLabel heading = AnthropicTheme.createHeadingLabel(
                "Cabin Seat Map - " + flight.getFlightNumber(), 22
        );
        JLabel routeInfo = AnthropicTheme.createMutedLabel(
                flight.getDepartureAirport().getAirportCode()
                        + " -> " + flight.getArrivalAirport().getAirportCode()
                        + "  |  Aircraft: " + flight.getAircraft().getModel()
                        + "  |  Base Fare: " + ReservationGuiController.formatCurrency(flight.getBasePrice())
        );

        titleBlock.add(heading);
        titleBlock.add(routeInfo);

        headerPanel.add(titleBlock, BorderLayout.NORTH);
        headerPanel.add(buildLegendRow(), BorderLayout.CENTER);

        // Seat Grid
        JPanel seatGrid = new JPanel(new GridLayout(0, CABIN_COLUMNS, AnthropicTheme.SPACE_12, AnthropicTheme.SPACE_12));
        seatGrid.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        seatGrid.setBorder(AnthropicTheme.spacing(AnthropicTheme.SPACE_8, AnthropicTheme.SPACE_8));

        for (Seat seat : controller.getSeatsForFlight(flight.getFlightId())) {
            seatGrid.add(createSeatToggleButton(seat));
        }

        JScrollPane seatScrollPane = new JScrollPane(seatGrid);
        seatScrollPane.setBorder(BorderFactory.createLineBorder(AnthropicTheme.PRIMARY_BORDER, 1));
        seatScrollPane.getViewport().setBackground(AnthropicTheme.PRIMARY_SURFACE);
        seatScrollPane.setPreferredSize(new Dimension(680, 360));

        // Summary & Actions Footer
        JPanel footerPanel = new JPanel(new BorderLayout(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_12));
        AnthropicTheme.styleSubtlePanel(footerPanel);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(AnthropicTheme.TERTIARY_SURFACE);

        summaryPanel.add(selectedSeatsLabel);
        summaryPanel.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        summaryPanel.add(totalPriceLabel);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AnthropicTheme.SPACE_12, 0));
        actionButtons.setBackground(AnthropicTheme.TERTIARY_SURFACE);

        JButton cancelButton = new JButton("Cancel");
        AnthropicTheme.styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());

        JButton confirmButton = new JButton("Confirm Booking");
        AnthropicTheme.stylePrimaryButton(confirmButton);
        confirmButton.addActionListener(e -> confirmBooking());

        actionButtons.add(cancelButton);
        actionButtons.add(confirmButton);

        footerPanel.add(summaryPanel, BorderLayout.CENTER);
        footerPanel.add(actionButtons, BorderLayout.EAST);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(seatScrollPane, BorderLayout.CENTER);
        root.add(footerPanel, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
        updateSelectionSummary();
        pack();
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(owner);
    }

    public Booking getCreatedBooking() {
        return createdBooking;
    }

    private JPanel buildLegendRow() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, AnthropicTheme.SPACE_8, AnthropicTheme.SPACE_8));
        legend.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        legend.add(AnthropicTheme.createOutlinedBadgeLabel(
                "Business (Lounge)", AnthropicTheme.TERTIARY_SURFACE, AnthropicTheme.PRIMARY_TEXT, AnthropicTheme.WARM_ACCENT));
        legend.add(AnthropicTheme.createBadgeLabel(
                "Economy+ Legroom", AnthropicTheme.SOFT_SEAFOAM, AnthropicTheme.PRIMARY_TEXT));
        legend.add(AnthropicTheme.createOutlinedBadgeLabel(
                "Economy", AnthropicTheme.PRIMARY_SURFACE, AnthropicTheme.PRIMARY_TEXT, AnthropicTheme.PRIMARY_BORDER));
        legend.add(AnthropicTheme.createBadgeLabel(
                "Selected", AnthropicTheme.WARM_ACCENT, AnthropicTheme.PRIMARY_CTA_TEXT));
        legend.add(AnthropicTheme.createBadgeLabel(
                "Reserved", AnthropicTheme.SOFT_BLUSH, AnthropicTheme.NEUTRAL_DARK));
        legend.add(AnthropicTheme.createBadgeLabel(
                "Occupied", AnthropicTheme.PRIMARY_BORDER, AnthropicTheme.NEUTRAL_DARK));
        return legend;
    }

    private JToggleButton createSeatToggleButton(Seat seat) {
        SeatPresentation presentation = describeSeat(seat);
        JToggleButton button = new JToggleButton();
        button.setLayout(new BorderLayout());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(145, 76));
        button.setToolTipText(presentation.tooltip());

        JLabel tileLabel = new JLabel(presentation.buttonHtml(), SwingConstants.CENTER);
        tileLabel.setFont(AnthropicTheme.sansFont(Font.PLAIN, 12));
        button.add(tileLabel, BorderLayout.CENTER);

        if (!seat.isAvailable()) {
            button.setEnabled(false);
            applySeatTileStyle(button, tileLabel, seat, presentation, false);
            return button;
        }

        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applySeatTileStyle(button, tileLabel, seat, presentation, false);
        button.addItemListener(e -> {
            if (button.isSelected()) {
                selectedSeatNumbers.add(seat.getSeatNumber());
            } else {
                selectedSeatNumbers.remove(seat.getSeatNumber());
            }
            applySeatTileStyle(button, tileLabel, seat, presentation, button.isSelected());
            updateSelectionSummary();
        });
        return button;
    }

    private SeatPresentation describeSeat(Seat seat) {
        double seatPrice = controller.previewSeatSelectionPrice(flight.getFlightId(), List.of(seat.getSeatNumber()));
        String priceText = ReservationGuiController.formatCurrency(seatPrice);

        String tierLine;
        String tooltipDetail;
        Color baseBg;
        Color baseBorder;

        if (seat instanceof BusinessSeat businessSeat) {
            String loungeTag = businessSeat.hasLoungeAccess() ? " · Lounge" : "";
            tierLine = "Business x" + businessSeat.getPriceMultiplier() + loungeTag;
            tooltipDetail = "Multiplier: " + businessSeat.getPriceMultiplier()
                    + "x | Lounge Access: " + (businessSeat.hasLoungeAccess() ? "Yes" : "No");
            baseBg = AnthropicTheme.TERTIARY_SURFACE;
            baseBorder = AnthropicTheme.WARM_ACCENT;
        } else if (seat instanceof EconomySeat economySeat && economySeat.hasExtraLegroom()) {
            tierLine = "Economy+ Legroom";
            tooltipDetail = "Extra Legroom";
            baseBg = AnthropicTheme.SOFT_SEAFOAM;
            baseBorder = AnthropicTheme.SAGE_ACCENT;
        } else {
            tierLine = "Economy";
            tooltipDetail = "Standard Cabin";
            baseBg = AnthropicTheme.PRIMARY_SURFACE;
            baseBorder = AnthropicTheme.PRIMARY_BORDER;
        }

        String statusOrPrice = seat.isAvailable() ? priceText : seat.getStatus().name();
        String html = "<html><center><b>" + seat.getSeatNumber() + "</b><br>"
                + tierLine + "<br>"
                + statusOrPrice + "</center></html>";

        String tooltip = seat.getSeatNumber() + " (" + seat.getSeatType() + ", " + seat.getStatus() + ") - "
                + priceText + " | " + tooltipDetail;

        return new SeatPresentation(html, tooltip, baseBg, baseBorder);
    }

    private void applySeatTileStyle(JToggleButton button,
                                    JLabel tileLabel,
                                    Seat seat,
                                    SeatPresentation presentation,
                                    boolean selected) {
        if (!seat.isAvailable()) {
            Color disabledBg = seat.getStatus() == SeatStatus.RESERVED
                    ? AnthropicTheme.SOFT_BLUSH
                    : AnthropicTheme.PRIMARY_BORDER;
            button.setBackground(disabledBg);
            tileLabel.setForeground(AnthropicTheme.NEUTRAL_DARK);
            button.setBorder(BorderFactory.createLineBorder(AnthropicTheme.SECONDARY_BORDER, 1));
            return;
        }

        if (selected) {
            button.setBackground(AnthropicTheme.WARM_ACCENT);
            tileLabel.setForeground(AnthropicTheme.PRIMARY_CTA_TEXT);
            button.setBorder(BorderFactory.createLineBorder(AnthropicTheme.EARTH_ACCENT, 2));
            return;
        }

        button.setBackground(presentation.baseBg());
        tileLabel.setForeground(AnthropicTheme.PRIMARY_TEXT);
        button.setBorder(BorderFactory.createLineBorder(presentation.baseBorder(), 1));
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
            JOptionPane.showMessageDialog(
                    this,
                    "Booking " + booking.getBookingId() + " created (" + booking.getStatus() + ").\n"
                            + "Flight: " + flight.getFlightNumber() + "\n"
                            + "Seats: " + String.join(", ", seatsToBook) + "\n"
                            + "Total Fare: " + ReservationGuiController.formatCurrency(booking.getTotalPrice()),
                    "Booking Hold Confirmed",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (AirlineReservationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Seat Booking Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private record SeatPresentation(String buttonHtml, String tooltip, Color baseBg, Color baseBorder) {
    }
}
