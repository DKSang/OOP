package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Person;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Role workspace container for authenticated {@code Passenger} sessions.
 */
public class PassengerWorkspacePanel extends AbstractWorkspacePanel {

    private final JComboBox<Airport> departureCombo = new JComboBox<>();
    private final JComboBox<Airport> arrivalCombo = new JComboBox<>();
    private final JTextField travelDateField = new JTextField();
    private final JTextField seatCountField = new JTextField("1");
    private final JLabel flightCountLabel = AnthropicTheme.createMutedLabel("Showing 0 flight(s)");

    private final DefaultTableModel flightTableModel = new DefaultTableModel(
            ReservationGuiController.FLIGHT_TABLE_COLUMNS, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable flightTable = new JTable(flightTableModel);

    private List<Flight> displayedFlights = List.of();

    public PassengerWorkspacePanel(ReservationGuiController controller) {
        super(
                controller,
                "Passenger Workspace",
                "Flight search, interactive seat selection, and booking payment tabs connect here."
        );
        travelDateField.setText(controller.getDefaultTravelDate().toString());
        tabs.addTab("Search & Book Flights", createSearchFlightsTab());
        tabs.addTab("My Bookings", createPlaceholderTab(
                "My Bookings",
                "Passenger booking history, payment retry, check-in, and cancellation will appear here."
        ));
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

    private JPanel createSearchFlightsTab() {
        JPanel tabPanel = new JPanel(new BorderLayout(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_16));
        tabPanel.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        tabPanel.setBorder(AnthropicTheme.spacing(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_16));

        tabPanel.add(createSearchFilterCard(), BorderLayout.NORTH);
        tabPanel.add(createFlightResultsPanel(), BorderLayout.CENTER);
        tabPanel.add(createBottomActionBar(), BorderLayout.SOUTH);
        return tabPanel;
    }

    private JPanel createSearchFilterCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        AnthropicTheme.styleCard(card);

        JLabel sectionTitle = AnthropicTheme.createSectionLabel("Find Available Flights");
        sectionTitle.setAlignmentX(LEFT_ALIGNMENT);

        JPanel fieldsGrid = new JPanel(new GridLayout(2, 4, AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_8));
        fieldsGrid.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        fieldsGrid.setAlignmentX(LEFT_ALIGNMENT);

        configureAirportCombo(departureCombo);
        configureAirportCombo(arrivalCombo);
        AnthropicTheme.styleTextInput(travelDateField);
        AnthropicTheme.styleTextInput(seatCountField);

        fieldsGrid.add(AnthropicTheme.createFieldLabel("Departure Airport"));
        fieldsGrid.add(AnthropicTheme.createFieldLabel("Arrival Airport"));
        fieldsGrid.add(AnthropicTheme.createFieldLabel("Travel Date (YYYY-MM-DD)"));
        fieldsGrid.add(AnthropicTheme.createFieldLabel("Seats Needed"));

        fieldsGrid.add(departureCombo);
        fieldsGrid.add(arrivalCombo);
        fieldsGrid.add(travelDateField);
        fieldsGrid.add(seatCountField);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsRow.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        actionsRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton searchButton = new JButton("Search Flights");
        AnthropicTheme.stylePrimaryButton(searchButton);
        searchButton.addActionListener(e -> executeFlightSearch());

        JButton resetButton = new JButton("Show All Scheduled Flights");
        AnthropicTheme.styleSecondaryButton(resetButton);
        resetButton.addActionListener(e -> resetFiltersAndShowAllScheduled());

        actionsRow.add(searchButton);
        actionsRow.add(Box.createHorizontalStrut(AnthropicTheme.SPACE_12));
        actionsRow.add(resetButton);

        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_12));
        card.add(fieldsGrid);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_16));
        card.add(actionsRow);
        return card;
    }

    private JScrollPane createFlightResultsPanel() {
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.setRowHeight(32);
        flightTable.setFont(AnthropicTheme.sansFont(Font.PLAIN, 13));
        flightTable.setForeground(AnthropicTheme.PRIMARY_TEXT);
        flightTable.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        flightTable.setGridColor(AnthropicTheme.PRIMARY_BORDER);
        flightTable.setSelectionBackground(AnthropicTheme.TERTIARY_SURFACE);
        flightTable.setSelectionForeground(AnthropicTheme.PRIMARY_TEXT);
        flightTable.setFillsViewportHeight(true);

        JTableHeader header = flightTable.getTableHeader();
        header.setFont(AnthropicTheme.sansFont(Font.BOLD, 13));
        header.setBackground(AnthropicTheme.TERTIARY_SURFACE);
        header.setForeground(AnthropicTheme.PRIMARY_TEXT);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(AnthropicTheme.PRIMARY_BORDER, 1));
        scrollPane.getViewport().setBackground(AnthropicTheme.PRIMARY_SURFACE);
        scrollPane.setPreferredSize(new Dimension(920, 260));
        return scrollPane;
    }

    private JPanel createBottomActionBar() {
        JPanel bar = new JPanel(new BorderLayout(AnthropicTheme.SPACE_16, 0));
        bar.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        JButton selectSeatsButton = new JButton("Select Seats & Book");
        AnthropicTheme.stylePrimaryButton(selectSeatsButton);
        selectSeatsButton.addActionListener(e -> openSeatSelectionDialog());

        bar.add(flightCountLabel, BorderLayout.WEST);
        bar.add(selectSeatsButton, BorderLayout.EAST);
        return bar;
    }

    private void populateAirportSelectors() {
        List<Airport> airports = controller.getAirports();
        departureCombo.removeAllItems();
        arrivalCombo.removeAllItems();
        for (Airport airport : airports) {
            departureCombo.addItem(airport);
            arrivalCombo.addItem(airport);
        }
        resetAirportSelections();
    }

    private void resetAirportSelections() {
        int count = departureCombo.getItemCount();
        if (count >= 2) {
            departureCombo.setSelectedIndex(0);
            arrivalCombo.setSelectedIndex(1);
        } else if (count == 1) {
            departureCombo.setSelectedIndex(0);
            arrivalCombo.setSelectedIndex(0);
        }
    }

    private void executeFlightSearch() {
        Airport dep = (Airport) departureCombo.getSelectedItem();
        Airport arr = (Airport) arrivalCombo.getSelectedItem();
        String depCode = dep == null ? "" : dep.getAirportCode();
        String arrCode = arr == null ? "" : arr.getAirportCode();

        try {
            List<Flight> matches = controller.searchFlights(
                    depCode,
                    arrCode,
                    travelDateField.getText(),
                    seatCountField.getText()
            );
            populateFlightTable(matches);
        } catch (AirlineReservationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Flight Search Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void resetFiltersAndShowAllScheduled() {
        resetAirportSelections();
        travelDateField.setText(controller.getDefaultTravelDate().toString());
        seatCountField.setText("1");
        populateFlightTable(controller.getScheduledFlights());
    }

    private void populateFlightTable(List<Flight> flights) {
        this.displayedFlights = List.copyOf(flights);
        flightTableModel.setRowCount(0);
        for (Object[] row : controller.toFlightTableRows(displayedFlights)) {
            flightTableModel.addRow(row);
        }
        flightCountLabel.setText("Showing " + displayedFlights.size() + " flight(s)");
    }

    private void openSeatSelectionDialog() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= displayedFlights.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a flight from the table first.",
                    "No Flight Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Flight flight = displayedFlights.get(selectedRow);
        try {
            SeatSelectionDialog dialog = new SeatSelectionDialog(
                    SwingUtilities.getWindowAncestor(this),
                    controller,
                    flight
            );
            dialog.setVisible(true);
            if (dialog.getCreatedBooking() != null) {
                populateFlightTable(displayedFlights);
            }
        } catch (AirlineReservationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Unable to Open Seat Map",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void configureAirportCombo(JComboBox<Airport> comboBox) {
        AnthropicTheme.styleComboBox(comboBox);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Airport airport) {
                    setText(airport.getAirportCode() + " - " + airport.getCity());
                    setToolTipText(airport.getAirportName());
                }
                return this;
            }
        });
    }
}
