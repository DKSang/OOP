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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;

/**
 * Modal JavaFX dialog allowing a new {@link Passenger} to register directly from the login view
 * with an automatically generated Passenger ID.
 */
public class PassengerRegistrationDialog extends Stage {

    private final ReservationGuiController controller;
    private Passenger registeredPassenger;

    private final Label autoIdBadgeLabel;
    private final TextField fullNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final DatePicker dobPicker = ArbitrumTheme.createIsoDatePicker(LocalDate.of(2000, 1, 1));
    private final ComboBox<String> genderCombo = new ComboBox<>();
    private final TextField passportField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    private final HBox errorBanner = new HBox(ArbitrumTheme.SPACE_8);
    private final Label errorLabel = new Label();

    public PassengerRegistrationDialog(Window owner, ReservationGuiController controller) {
        this.controller = controller;

        String nextId = controller.generateNextPassengerId();
        this.autoIdBadgeLabel = ArbitrumTheme.createBadgeLabel("AUTO ID · " + nextId, "badge-passenger");

        setTitle("Create Passenger Account");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        VBox root = new VBox(ArbitrumTheme.SPACE_16);
        root.getStyleClass().add("bg-repeating-lines");
        root.setPadding(new Insets(ArbitrumTheme.SPACE_24));

        VBox headerCopy = new VBox(
                4,
                ArbitrumTheme.createCyanEyebrowLabel("PASSENGER ONBOARDING"),
                ArbitrumTheme.createDisplayTitle("Create Passenger Profile", "display-md"),
                ArbitrumTheme.createHeroSubtitle(
                        "Passenger ID (" + nextId + ") is assigned automatically upon registration."
                )
        );
        BorderPane headerBanner = new BorderPane();
        headerBanner.getStyleClass().add("workspace-banner");
        headerBanner.setLeft(headerCopy);
        headerBanner.setRight(autoIdBadgeLabel);
        BorderPane.setAlignment(autoIdBadgeLabel, Pos.TOP_RIGHT);

        VBox formCard = new VBox(ArbitrumTheme.SPACE_16);
        formCard.getStyleClass().add("card");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(ArbitrumTheme.SPACE_16);
        formGrid.setVgap(12);

        for (int i = 0; i < 2; i++) {
            ColumnConstraints col = new ColumnConstraints(260);
            col.setPercentWidth(50);
            col.setHgrow(Priority.ALWAYS);
            formGrid.getColumnConstraints().add(col);
        }

        ArbitrumTheme.styleTextInput(fullNameField);
        fullNameField.setPromptText("e.g. Pham Gia Huy");
        ArbitrumTheme.styleTextInput(emailField);
        emailField.setPromptText("e.g. huy.pham@airline.vn");
        ArbitrumTheme.styleTextInput(phoneField);
        phoneField.setPromptText("e.g. 0903334444");
        ArbitrumTheme.styleTextInput(passportField);
        passportField.setPromptText("e.g. CCCD-0003");
        ArbitrumTheme.styleTextInput(passwordField);
        passwordField.setPromptText("Enter account password");
        passwordField.setOnAction(e -> submitRegistration());

        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.getSelectionModel().selectFirst();
        ArbitrumTheme.styleComboBox(genderCombo);

        formGrid.add(labeledField("Full Name", fullNameField), 0, 0);
        formGrid.add(labeledField("Passport / CCCD", passportField), 1, 0);
        formGrid.add(labeledField("Email Address", emailField), 0, 1);
        formGrid.add(labeledField("Phone Number", phoneField), 1, 1);
        formGrid.add(labeledField("Date of Birth (YYYY-MM-DD)", dobPicker), 0, 2);
        formGrid.add(labeledField("Gender", genderCombo), 1, 2);
        formGrid.add(labeledField("Account Password", passwordField), 0, 3, 2, 1);

        errorBanner.getStyleClass().addAll("status-banner", "status-banner-error");
        errorBanner.setAlignment(Pos.CENTER_LEFT);
        errorBanner.managedProperty().bind(errorBanner.visibleProperty());
        errorBanner.setVisible(false);
        errorLabel.getStyleClass().add("status-banner-text");
        errorLabel.setWrapText(true);
        errorBanner.getChildren().setAll(errorLabel);

        Button autoFillButton = ArbitrumTheme.createSecondaryButton("Auto-Fill Sample");
        autoFillButton.setOnAction(e -> fillSamplePassenger());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelButton = ArbitrumTheme.createSecondaryButton("Cancel");
        cancelButton.setOnAction(e -> close());

        Button submitButton = ArbitrumTheme.createPrimaryButton("Create Account");
        submitButton.setOnAction(e -> submitRegistration());

        HBox actions = new HBox(ArbitrumTheme.SPACE_12, autoFillButton, spacer, cancelButton, submitButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        formCard.getChildren().addAll(formGrid, errorBanner, actions);
        root.getChildren().addAll(headerBanner, formCard);

        Scene scene = new Scene(root, 640, 510);
        ArbitrumTheme.applyStylesheet(scene);
        setScene(scene);
        sizeToScene();
    }

    public Passenger getRegisteredPassenger() {
        return registeredPassenger;
    }

    private void fillSamplePassenger() {
        int nextSeq = controller.getPortalMetrics().airportsCount() + (int) (System.currentTimeMillis() % 89) + 10;
        fullNameField.setText("Pham Gia Huy");
        emailField.setText("huy." + nextSeq + "@airline.vn");
        phoneField.setText("09033344" + nextSeq);
        dobPicker.setValue(LocalDate.of(2002, 7, 15));
        genderCombo.getSelectionModel().select("Male");
        passportField.setText("CCCD-00" + nextSeq);
        passwordField.setText("pass123");
        errorBanner.setVisible(false);
    }

    private void submitRegistration() {
        try {
            errorBanner.setVisible(false);
            registeredPassenger = controller.registerPassenger(
                    fullNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    dobPicker.getEditor().getText(),
                    genderCombo.getSelectionModel().getSelectedItem(),
                    passportField.getText(),
                    passwordField.getText()
            );
            close();
        } catch (AirlineReservationException ex) {
            errorLabel.setText(ex.getMessage());
            errorBanner.setVisible(true);
        }
    }

    private static VBox labeledField(String labelText, Node input) {
        return new VBox(6, ArbitrumTheme.createFieldLabel(labelText), input);
    }
}
