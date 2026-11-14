package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Web3 split-hero login portal pairing an Arbitrum gradient showcase panel with live system metrics,
 * 1-click demo session chips, and an elevated sign-in card with inline feedback.
 */
public class LoginView extends StackPane {

    private final ReservationGuiController controller;
    private final Consumer<Person> onLoginSuccess;
    private final Runnable onRegisterRequested;

    private final TextField identifierField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    private final HBox inlineFeedbackBox = new HBox(ArbitrumTheme.SPACE_8);
    private final Label inlineFeedbackLabel = new Label();

    private final Label airportsMetricLabel = createMetricValueLabel();
    private final Label flightsMetricLabel = createMetricValueLabel();
    private final Label seatsMetricLabel = createMetricValueLabel();

    public LoginView(ReservationGuiController controller,
                     Consumer<Person> onLoginSuccess,
                     Runnable onRegisterRequested) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
        this.onRegisterRequested = onRegisterRequested;

        setPadding(new Insets(ArbitrumTheme.SPACE_24));
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
        identifierField.clear();
        passwordField.clear();
        hideFeedback();
    }

    public void prefillIdentifier(String identifier) {
        identifierField.setText(identifier == null ? "" : identifier);
        passwordField.clear();
        showFeedback(
                "Account " + identifier + " registered. Enter your password to sign in.",
                "status-banner-success"
        );
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
        hero.setMinHeight(460);

        Label eyebrow = ArbitrumTheme.createCyanEyebrowLabel("ARBITRUM AVIATION NETWORK");
        Label headline = ArbitrumTheme.createDisplayTitle("POWERING ONCHAIN AVIATION", "display-xl");
        headline.setWrapText(true);

        Label lead = ArbitrumTheme.createHeroSubtitle(
                "Deterministic flight scheduling, multi-tier cabin seat allocation, and instant booking "
                        + "settlement across Vietnam's core aviation corridors."
        );
        lead.setMaxWidth(520);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label metricsEyebrow = ArbitrumTheme.createCyanEyebrowLabel("LIVE NETWORK TELEMETRY");
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
        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMinWidth(420);
        card.setPrefWidth(450);
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("PORTAL AUTHENTICATION");
        Label titleLabel = ArbitrumTheme.createHeadingLabel("Sign In", "heading-xl");
        Label subtitleLabel = ArbitrumTheme.createMutedLabel(
                "Enter your Passenger ID, Email, Passport, or Admin ID — or pick a demo account below."
        );
        subtitleLabel.setWrapText(true);

        VBox headerBlock = new VBox(6, eyebrow, titleLabel, subtitleLabel);

        FlowPane demoChipsRow = new FlowPane(ArbitrumTheme.SPACE_8, ArbitrumTheme.SPACE_8);
        for (DemoAccount account : controller.getDemoAccounts()) {
            Button chip = new Button(account.identifier() + " · " + account.role());
            chip.getStyleClass().add("demo-chip");
            chip.setTooltip(new Tooltip("Click to fill " + account.label() + " (Double-click to sign in immediately)"));
            chip.setOnAction(e -> {
                identifierField.setText(account.identifier());
                passwordField.setText(account.password());
                hideFeedback();
            });
            chip.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    identifierField.setText(account.identifier());
                    passwordField.setText(account.password());
                    attemptLogin();
                }
            });
            demoChipsRow.getChildren().add(chip);
        }

        ArbitrumTheme.styleTextInput(identifierField);
        identifierField.setPromptText("e.g. PAX-01, email, or ADM-01");
        identifierField.setOnAction(e -> attemptLogin());

        ArbitrumTheme.styleTextInput(passwordField);
        passwordField.setPromptText("Enter password");
        passwordField.setOnAction(e -> attemptLogin());

        configureInlineFeedback();

        Button loginButton = ArbitrumTheme.createPrimaryButton("Sign In");
        loginButton.setOnAction(e -> attemptLogin());

        Button registerButton = ArbitrumTheme.createSecondaryButton("Create Passenger Account");
        registerButton.setOnAction(e -> onRegisterRequested.run());

        HBox buttonRow = new HBox(ArbitrumTheme.SPACE_12, loginButton, registerButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(buttonRow, new Insets(4, 0, 0, 0));

        card.getChildren().addAll(
                headerBlock,
                labeledGroup("Quick Demo Accounts (Click to Fill · Double-Click to Sign In)", demoChipsRow),
                labeledGroup("User ID / Email / Staff Code / Passport", identifierField),
                labeledGroup("Password", passwordField),
                inlineFeedbackBox,
                buttonRow
        );
        return card;
    }

    private void configureInlineFeedback() {
        inlineFeedbackBox.getStyleClass().addAll("status-banner", "status-banner-error");
        inlineFeedbackBox.setAlignment(Pos.CENTER_LEFT);
        inlineFeedbackBox.managedProperty().bind(inlineFeedbackBox.visibleProperty());
        inlineFeedbackBox.setVisible(false);

        inlineFeedbackLabel.getStyleClass().add("status-banner-text");
        inlineFeedbackLabel.setWrapText(true);
        inlineFeedbackBox.getChildren().setAll(inlineFeedbackLabel);
    }

    private void showFeedback(String message, String variantClass) {
        inlineFeedbackBox.getStyleClass().removeAll(
                "status-banner-info",
                "status-banner-success",
                "status-banner-warning",
                "status-banner-error"
        );
        inlineFeedbackBox.getStyleClass().add(variantClass);
        inlineFeedbackLabel.setText(message);
        inlineFeedbackBox.setVisible(true);
    }

    private void hideFeedback() {
        inlineFeedbackBox.setVisible(false);
    }

    private void attemptLogin() {
        try {
            hideFeedback();
            Person user = controller.login(identifierField.getText(), passwordField.getText());
            onLoginSuccess.accept(user);
        } catch (AirlineReservationException ex) {
            showFeedback(ex.getMessage(), "status-banner-error");
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
        return new VBox(6, ArbitrumTheme.createFieldLabel(labelText), control);
    }
}
