package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
 * Login view supporting manual credential entry, one-click demo account quick-fill,
 * and launching passenger registration.
 */
public class LoginView extends StackPane {

    private static final String QUICK_FILL_PROMPT = "-- Quick Fill Demo Account --";

    private final ReservationGuiController controller;
    private final Consumer<Person> onLoginSuccess;
    private final Runnable onRegisterRequested;

    private final ComboBox<Object> quickFillCombo = new ComboBox<>();
    private final TextField identifierField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    public LoginView(ReservationGuiController controller,
                     Consumer<Person> onLoginSuccess,
                     Runnable onRegisterRequested) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
        this.onRegisterRequested = onRegisterRequested;

        setPadding(new Insets(AnthropicTheme.SPACE_24));
        setAlignment(Pos.CENTER);

        VBox card = new VBox(AnthropicTheme.SPACE_16);
        card.getStyleClass().add("card");
        card.setMaxWidth(450);
        card.setPrefWidth(450);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        Label titleLabel = AnthropicTheme.createHeadingLabel("Sign In", "heading-xl");
        Label subtitleLabel = AnthropicTheme.createMutedLabel(
                "Sign in with your Passenger ID / Email / Passport or Admin ID / Staff Code."
        );
        subtitleLabel.setWrapText(true);

        VBox headerBlock = new VBox(AnthropicTheme.SPACE_8, titleLabel, subtitleLabel);

        quickFillCombo.getItems().add(QUICK_FILL_PROMPT);
        quickFillCombo.getItems().addAll(controller.getDemoAccounts());
        quickFillCombo.getSelectionModel().selectFirst();
        AnthropicTheme.styleComboBox(quickFillCombo);
        quickFillCombo.setOnAction(e -> applyQuickFillSelection());

        AnthropicTheme.styleTextInput(identifierField);
        identifierField.setPromptText("e.g. PAX-01, email, or ADM-01");

        AnthropicTheme.styleTextInput(passwordField);
        passwordField.setPromptText("Enter password");
        passwordField.setOnAction(e -> attemptLogin());

        Button loginButton = AnthropicTheme.createPrimaryButton("Login");
        loginButton.setOnAction(e -> attemptLogin());

        Button registerButton = AnthropicTheme.createSecondaryButton("Register Passenger");
        registerButton.setOnAction(e -> this.onRegisterRequested.run());

        HBox buttonRow = new HBox(AnthropicTheme.SPACE_12, loginButton, registerButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(loginButton, Priority.NEVER);
        VBox.setMargin(buttonRow, new Insets(AnthropicTheme.SPACE_8, 0, 0, 0));

        card.getChildren().addAll(
                headerBlock,
                labeledGroup("Demo Quick Fill", quickFillCombo),
                labeledGroup("User ID / Email / Staff Code / Passport", identifierField),
                labeledGroup("Password", passwordField),
                buttonRow
        );

        getChildren().add(card);
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
            AnthropicTheme.showError(
                    getScene() == null ? null : getScene().getWindow(),
                    "Login Failed",
                    ex.getMessage()
            );
        }
    }

    private static VBox labeledGroup(String labelText, javafx.scene.Node control) {
        VBox group = new VBox(AnthropicTheme.SPACE_8, AnthropicTheme.createFieldLabel(labelText), control);
        return group;
    }
}
