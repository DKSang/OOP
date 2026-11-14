package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Web3 split-hero login portal pairing an Arbitrum gradient showcase panel with live system metrics
 * and an elevated sign-in card.
 */
public class LoginView extends StackPane {

    private static final String QUICK_FILL_PROMPT = "-- Quick Fill Demo Account --";

    private final ReservationGuiController controller;
    private final Consumer<Person> onLoginSuccess;
    private final Runnable onRegisterRequested;

    private final ComboBox<Object> quickFillCombo = new ComboBox<>();
    private final TextField identifierField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    private final Label airportsMetricLabel = createMetricValueLabel();
    private final Label flightsMetricLabel = createMetricValueLabel();
    private final Label seatsMetricLabel = createMetricValueLabel();

    public LoginView(ReservationGuiController controller,
                     Consumer<Person> onLoginSuccess,
                     Runnable onRegisterRequested) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
        this.onRegisterRequested = onRegisterRequested;

        setPadding(new Insets(ArbitrumTheme.SPACE_32));
        setAlignment(Pos.CENTER);

        VBox heroPanel = buildHeroShowcasePanel();
        VBox signInCard = buildSignInCard();

        HBox.setHgrow(heroPanel, Priority.ALWAYS);
        HBox.setHgrow(signInCard, Priority.NEVER);

        HBox splitContainer = new HBox(ArbitrumTheme.SPACE_24, heroPanel, signInCard);
        splitContainer.setAlignment(Pos.CENTER);
        splitContainer.setMaxWidth(1160);
        splitContainer.setMaxHeight(Region.USE_PREF_SIZE);

        refreshMetrics();
        getChildren().add(splitContainer);
    }

    public void clearInputs() {
        quickFillCombo.getSelectionModel().selectFirst();
        identifierField.clear();
        passwordField.clear();
    }

    public void prefillIdentifier(String identifier) {
        identifierField.setText(identifier == null ? "" : identifier);
        passwordField.clear();
        passwordField.requestFocus();
    }

    public void refreshMetrics() {
        ReservationGuiController.PortalMetrics metrics = controller.getPortalMetrics();
        airportsMetricLabel.setText(String.valueOf(metrics.airportsCount()));
        flightsMetricLabel.setText(String.valueOf(metrics.scheduledFlightsCount()));
        seatsMetricLabel.setText(String.valueOf(metrics.availableSeatsCount()));
    }

    private VBox buildHeroShowcasePanel() {
        VBox hero = new VBox(ArbitrumTheme.SPACE_16);
        hero.getStyleClass().add("hero-banner");
        hero.setMinWidth(460);
        hero.setPrefWidth(640);
        hero.setMaxWidth(680);
        hero.setMinHeight(470);

        Label eyebrow = ArbitrumTheme.createCyanEyebrowLabel("ARBITRUM AVIATION NETWORK // LIVE");
        Label headline = ArbitrumTheme.createDisplayTitle("POWERING ONCHAIN AVIATION", "display-xl");
        headline.setWrapText(true);

        Label lead = ArbitrumTheme.createHeroSubtitle(
                "Deterministic flight scheduling, multi-tier cabin seat allocation, and instant booking "
                        + "settlement across Vietnam's core aviation corridors."
        );
        lead.setMaxWidth(520);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label metricsEyebrow = ArbitrumTheme.createCyanEyebrowLabel("LIVE SYSTEM METRICS");
        HBox metricsRow = new HBox(
                ArbitrumTheme.SPACE_12,
                buildMetricTile("AIRPORTS", airportsMetricLabel),
                buildMetricTile("SCHEDULED FLIGHTS", flightsMetricLabel),
                buildMetricTile("OPEN CABIN SEATS", seatsMetricLabel)
        );

        VBox metricsSection = new VBox(ArbitrumTheme.SPACE_8, metricsEyebrow, metricsRow);
        hero.getChildren().addAll(eyebrow, headline, lead, spacer, metricsSection);
        return hero;
    }

    private VBox buildSignInCard() {
        VBox card = new VBox(ArbitrumTheme.SPACE_16);
        card.getStyleClass().add("card");
        card.setMinWidth(420);
        card.setPrefWidth(450);
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("DAPP SESSION AUTH");
        Label titleLabel = ArbitrumTheme.createHeadingLabel("Sign In", "heading-xl");
        Label subtitleLabel = ArbitrumTheme.createMutedLabel(
                "Connect with your Passenger ID / Email / Passport or Admin ID / Staff Code."
        );
        subtitleLabel.setWrapText(true);

        VBox headerBlock = new VBox(ArbitrumTheme.SPACE_8, eyebrow, titleLabel, subtitleLabel);

        quickFillCombo.getItems().add(QUICK_FILL_PROMPT);
        quickFillCombo.getItems().addAll(controller.getDemoAccounts());
        quickFillCombo.getSelectionModel().selectFirst();
        ArbitrumTheme.styleComboBox(quickFillCombo);
        quickFillCombo.setOnAction(e -> applyQuickFillSelection());

        ArbitrumTheme.styleTextInput(identifierField);
        identifierField.setPromptText("e.g. PAX-01, email, or ADM-01");

        ArbitrumTheme.styleTextInput(passwordField);
        passwordField.setPromptText("Enter password");
        passwordField.setOnAction(e -> attemptLogin());

        Button loginButton = ArbitrumTheme.createPrimaryButton("Login");
        loginButton.setOnAction(e -> attemptLogin());

        Button registerButton = ArbitrumTheme.createSecondaryButton("Register Passenger");
        registerButton.setOnAction(e -> onRegisterRequested.run());

        HBox buttonRow = new HBox(ArbitrumTheme.SPACE_12, loginButton, registerButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(loginButton, Priority.NEVER);
        VBox.setMargin(buttonRow, new Insets(ArbitrumTheme.SPACE_8, 0, 0, 0));

        card.getChildren().addAll(
                headerBlock,
                labeledGroup("Demo Quick Fill", quickFillCombo),
                labeledGroup("User ID / Email / Staff Code / Passport", identifierField),
                labeledGroup("Password", passwordField),
                buttonRow
        );
        return card;
    }

    private void applyQuickFillSelection() {
        Object selected = quickFillCombo.getSelectionModel().getSelectedItem();
        if (selected instanceof DemoAccount account) {
            identifierField.setText(account.identifier());
            passwordField.setText(account.password());
        }
    }

    private void attemptLogin() {
        try {
            Person user = controller.login(identifierField.getText(), passwordField.getText());
            onLoginSuccess.accept(user);
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(
                    getScene() == null ? null : getScene().getWindow(),
                    "Login Failed",
                    ex.getMessage()
            );
        }
    }

    private static Label createMetricValueLabel() {
        Label label = new Label("0");
        label.getStyleClass().add("metric-value");
        return label;
    }

    private static VBox buildMetricTile(String caption, Label valueLabel) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("metric-label");
        VBox tile = new VBox(4, valueLabel, captionLabel);
        tile.getStyleClass().add("metric-tile");
        HBox.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    private static VBox labeledGroup(String labelText, Node control) {
        return new VBox(ArbitrumTheme.SPACE_8, ArbitrumTheme.createFieldLabel(labelText), control);
    }
}
