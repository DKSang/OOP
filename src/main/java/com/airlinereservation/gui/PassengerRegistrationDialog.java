package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Passenger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;

/**
 * Modal JavaFX dialog allowing a new {@link Passenger} to register directly from the login view.
 */
public class PassengerRegistrationDialog extends Stage {

    private final ReservationGuiController controller;
    private Passenger registeredPassenger;

    private final TextField idField = new TextField();
    private final TextField fullNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final DatePicker dobPicker = AnthropicTheme.createIsoDatePicker(LocalDate.of(2000, 1, 1));
    private final ComboBox<String> genderCombo = new ComboBox<>();
    private final TextField passportField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    public PassengerRegistrationDialog(Window owner, ReservationGuiController controller) {
        this.controller = controller;

        setTitle("Register New Passenger");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        VBox root = new VBox(AnthropicTheme.SPACE_16);
        root.setPadding(new Insets(AnthropicTheme.SPACE_24));

        Label heading = AnthropicTheme.createHeadingLabel("Passenger Registration", "heading-lg");
        Label subtitle = AnthropicTheme.createMutedLabel(
                "Create a passenger profile to search flights and manage bookings."
        );

        GridPane formGrid = new GridPane();
        formGrid.setHgap(AnthropicTheme.SPACE_16);
        formGrid.setVgap(AnthropicTheme.SPACE_12);

        ColumnConstraints labelColumn = new ColumnConstraints(185);
        ColumnConstraints inputColumn = new ColumnConstraints(270);
        inputColumn.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(labelColumn, inputColumn);

        AnthropicTheme.styleTextInput(idField);
        idField.setPromptText("PAX-03");
        AnthropicTheme.styleTextInput(fullNameField);
        AnthropicTheme.styleTextInput(emailField);
        AnthropicTheme.styleTextInput(phoneField);
        AnthropicTheme.styleTextInput(passportField);
        AnthropicTheme.styleTextInput(passwordField);

        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.getSelectionModel().selectFirst();
        AnthropicTheme.styleComboBox(genderCombo);

        addRow(formGrid, 0, "Passenger ID (e.g. PAX-03)", idField);
        addRow(formGrid, 1, "Full Name", fullNameField);
        addRow(formGrid, 2, "Email", emailField);
        addRow(formGrid, 3, "Phone Number", phoneField);
        addRow(formGrid, 4, "Date of Birth (YYYY-MM-DD)", dobPicker);
        addRow(formGrid, 5, "Gender", genderCombo);
        addRow(formGrid, 6, "Passport / CCCD", passportField);
        addRow(formGrid, 7, "Password", passwordField);

        Button cancelButton = AnthropicTheme.createSecondaryButton("Cancel");
        cancelButton.setOnAction(e -> close());

        Button submitButton = AnthropicTheme.createPrimaryButton("Register Passenger");
        submitButton.setOnAction(e -> submitRegistration());

        HBox actions = new HBox(AnthropicTheme.SPACE_12, cancelButton, submitButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(actions, new Insets(AnthropicTheme.SPACE_8, 0, 0, 0));

        root.getChildren().addAll(heading, subtitle, formGrid, actions);

        Scene scene = new Scene(root);
        AnthropicTheme.applyStylesheet(scene);
        setScene(scene);
        sizeToScene();
    }

    public Passenger getRegisteredPassenger() {
        return registeredPassenger;
    }

    private void submitRegistration() {
        try {
            registeredPassenger = controller.registerPassenger(
                    idField.getText(),
                    fullNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    dobPicker.getEditor().getText(),
                    genderCombo.getSelectionModel().getSelectedItem(),
                    passportField.getText(),
                    passwordField.getText()
            );
            AnthropicTheme.showInfo(
                    this,
                    "Registration Complete",
                    "Registered passenger " + registeredPassenger.getFullName()
                            + " (" + registeredPassenger.getId() + "). You can now log in."
            );
            close();
        } catch (AirlineReservationException ex) {
            AnthropicTheme.showError(this, "Registration Failed", ex.getMessage());
        }
    }

    private static void addRow(GridPane grid, int rowIndex, String labelText, Node input) {
        grid.add(AnthropicTheme.createFieldLabel(labelText), 0, rowIndex);
        grid.add(input, 1, rowIndex);
    }
}
