package com.airlinereservation.gui;

import com.airlinereservation.enums.BookingStatus;
import com.airlinereservation.enums.FlightStatus;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.model.Airport;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Flight;
import com.airlinereservation.model.Person;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Shared role-workspace shell providing an Arbitrum Hero Gradient banner and segmented pill tabs.
 */
abstract class AbstractWorkspaceView extends BorderPane {

    protected final ReservationGuiController controller;
    protected final TabPane tabs = new TabPane();
    private final Label welcomeLabel;
    private final String defaultTitle;

    protected AbstractWorkspaceView(ReservationGuiController controller,
                                    String eyebrow,
                                    String defaultTitle,
                                    String subtitle) {
        this.controller = controller;
        this.defaultTitle = defaultTitle;

        setPadding(new Insets(ArbitrumTheme.SPACE_24, ArbitrumTheme.SPACE_32, ArbitrumTheme.SPACE_24, ArbitrumTheme.SPACE_32));

        Label eyebrowLabel = ArbitrumTheme.createCyanEyebrowLabel(eyebrow);
        welcomeLabel = ArbitrumTheme.createDisplayTitle(defaultTitle, "display-lg");
        welcomeLabel.setWrapText(true);

        Label noteLabel = ArbitrumTheme.createHeroSubtitle(subtitle);

        VBox banner = new VBox(ArbitrumTheme.SPACE_8, eyebrowLabel, welcomeLabel, noteLabel);
        banner.getStyleClass().add("hero-banner");
        BorderPane.setMargin(banner, new Insets(0, 0, ArbitrumTheme.SPACE_24, 0));

        tabs.getStyleClass().add("workspace-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        setTop(banner);
        setCenter(tabs);
    }

    public void refreshData() {
        Person user = controller.getCurrentUser();
        String title = user != null ? formatWelcomeTitle(user) : defaultTitle;
        welcomeLabel.setText(title.toUpperCase(Locale.ROOT));
    }

    protected abstract String formatWelcomeTitle(Person user);

    protected Window ownerWindow() {
        return getScene() == null ? null : getScene().getWindow();
    }

    protected static Tab createNonClosableTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    protected static List<TableColumn<Flight, ?>> buildFlightExplorerColumns() {
        String[] cols = ReservationGuiController.FLIGHT_TABLE_COLUMNS;
        return List.of(
                textColumn(cols[0], Flight::getFlightId, 78, true),
                textColumn(cols[1], Flight::getFlightNumber, 84, true),
                textColumn(cols[2], ReservationGuiController::formatRoute, 110, true),
                textColumn(cols[3], f -> ReservationGuiController.formatDateTime(f.getDepartureTime()), 140, true),
                textColumn(cols[4], f -> ReservationGuiController.formatDateTime(f.getArrivalTime()), 140, true),
                textColumn(cols[5], f -> f.getAircraft().getModel(), 130, false),
                textColumn(cols[6], f -> String.valueOf(f.getAvailableSeats().size()), 110, true),
                textColumn(cols[7], f -> ReservationGuiController.formatCurrency(f.getBasePrice()), 130, true),
                flightStatusBadgeColumn(cols[8], 125)
        );
    }

    protected static <T> void configureExplorerTable(TableView<T> table, String emptyMessage) {
        table.getStyleClass().add("flight-table");
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(ArbitrumTheme.createMutedLabel(emptyMessage));
    }

    protected static <T> void refreshTableItems(TableView<T> table, List<T> items, Function<T, String> idExtractor) {
        T previous = table.getSelectionModel().getSelectedItem();
        String previousId = previous == null ? null : idExtractor.apply(previous);
        table.getItems().setAll(items);
        table.refresh();
        if (previousId != null) {
            for (T item : items) {
                if (previousId.equals(idExtractor.apply(item))) {
                    table.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    protected static void selectDefaultAirportPair(ComboBox<Airport> departureCombo, ComboBox<Airport> arrivalCombo) {
        int count = departureCombo.getItems().size();
        if (count >= 2) {
            departureCombo.getSelectionModel().select(0);
            arrivalCombo.getSelectionModel().select(1);
        } else if (count == 1) {
            departureCombo.getSelectionModel().select(0);
            arrivalCombo.getSelectionModel().select(0);
        }
    }

    protected static <T> TableColumn<T, String> textColumn(String title,
                                                           Function<T, String> extractor,
                                                           double minWidth,
                                                           boolean monospace) {
        TableColumn<T, String> col = new TableColumn<>(title.toUpperCase(Locale.ROOT));
        col.setMinWidth(minWidth);
        col.setCellValueFactory(cell -> new ReadOnlyStringWrapper(extractor.apply(cell.getValue())));
        if (monospace) {
            col.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().remove("explorer-mono-cell");
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        getStyleClass().add("explorer-mono-cell");
                    }
                }
            });
        }
        return col;
    }

    protected static TableColumn<Flight, FlightStatus> flightStatusBadgeColumn(String title, double minWidth) {
        return badgeColumn(
                title,
                minWidth,
                Flight::getStatus,
                Enum::name,
                status -> switch (status) {
                    case SCHEDULED -> "badge-status-scheduled";
                    case BOARDING -> "badge-status-warning";
                    case CANCELLED -> "badge-status-danger";
                    case DEPARTED, ARRIVED -> "badge-guest";
                },
                false
        );
    }

    protected static TableColumn<Booking, BookingStatus> bookingStatusBadgeColumn(String title, double minWidth) {
        return badgeColumn(
                title,
                minWidth,
                Booking::getStatus,
                Enum::name,
                status -> switch (status) {
                    case PENDING -> "badge-status-warning";
                    case CONFIRMED -> "badge-status-scheduled";
                    case CHECKED_IN -> "badge-legroom";
                    case COMPLETED -> "badge-business";
                    case CANCELLED -> "badge-status-danger";
                },
                false
        );
    }

    protected TableColumn<Booking, PaymentStatus> paymentStatusBadgeColumn(String title, double minWidth) {
        return badgeColumn(
                title,
                minWidth,
                controller::getBookingPaymentStatus,
                status -> status == null ? "UNPAID" : status.name(),
                status -> {
                    if (status == null) {
                        return "badge-guest";
                    }
                    return switch (status) {
                        case COMPLETED -> "badge-status-scheduled";
                        case PENDING -> "badge-status-warning";
                        case FAILED -> "badge-status-danger";
                        case REFUNDED -> "badge-business";
                    };
                },
                true
        );
    }

    protected static <T, S> TableColumn<T, S> badgeColumn(String title,
                                                          double minWidth,
                                                          Function<T, S> extractor,
                                                          Function<S, String> textMapper,
                                                          Function<S, String> styleMapper,
                                                          boolean renderNullValue) {
        TableColumn<T, S> col = new TableColumn<>(title.toUpperCase(Locale.ROOT));
        col.setMinWidth(minWidth);
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(extractor.apply(cell.getValue())));
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(S value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || (!renderNullValue && value == null)) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(ArbitrumTheme.createBadgeLabel(textMapper.apply(value), styleMapper.apply(value)));
                    setText(null);
                }
            }
        });
        return col;
    }

    protected static void configureAirportCombo(ComboBox<Airport> comboBox) {
        ArbitrumTheme.styleComboBox(comboBox);
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
