package com.airlinereservation.gui;

import com.airlinereservation.enums.PaymentMethod;
import com.airlinereservation.enums.PaymentStatus;
import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Booking;
import com.airlinereservation.model.Payment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.EnumMap;
import java.util.Map;

/**
 * Modal JavaFX payment gateway dialog supporting all {@link PaymentMethod} strategies
 * and simulated payment failure/retry workflows.
 */
public class PaymentDialog extends Stage {

    private final ReservationGuiController controller;
    private final Booking booking;
    private final Runnable onPaymentAttempted;

    private final ToggleGroup methodGroup = new ToggleGroup();
    private final Map<PaymentMethod, MethodSpec> methodSpecs = new EnumMap<>(PaymentMethod.class);

    private final Label primaryFieldLabel = ArbitrumTheme.createFieldLabel("");
    private final TextField primaryField = new TextField();
    private final Label secondaryFieldLabel = ArbitrumTheme.createFieldLabel("");
    private final TextField secondaryField = new TextField();
    private final CheckBox simulateFailureCheckBox = new CheckBox("Simulate Payment Failure (Demo Retry Flow)");
    private final Label attemptStatusLabel = ArbitrumTheme.createMonoLabel("");

    public PaymentDialog(Window owner,
                         ReservationGuiController controller,
                         Booking booking,
                         Runnable onPaymentAttempted) {
        this.controller = controller;
        this.booking = booking;
        this.onPaymentAttempted = onPaymentAttempted;

        initMethodSpecs();

        setTitle("Pay Booking - " + booking.getBookingId());
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        VBox root = new VBox(ArbitrumTheme.SPACE_16);
        root.getStyleClass().add("bg-repeating-lines");
        root.setPadding(new Insets(ArbitrumTheme.SPACE_24));

        VBox heroBanner = buildHeroBanner();
        VBox methodCard = buildMethodSelectionCard();
        VBox detailsCard = buildDetailsCard();
        BorderPane footerBar = buildFooterBar();

        root.getChildren().addAll(heroBanner, methodCard, detailsCard, footerBar);

        applyMethodFields(PaymentMethod.CREDIT_DEBIT_CARD);

        Scene scene = new Scene(root, 780, 650);
        ArbitrumTheme.applyStylesheet(scene);
        setScene(scene);
        sizeToScene();
    }

    private void initMethodSpecs() {
        String passengerName = booking.getPassenger().getFullName();
        String passengerPhone = booking.getPassenger().getPhoneNumber();

        methodSpecs.put(PaymentMethod.CREDIT_DEBIT_CARD, new MethodSpec(
                "CARD // VISA / MC",
                "Credit / Debit Card",
                "Instant card authorization",
                "Card Number", "4111-2222-3333-4444", "4111-2222-3333-4444",
                "Card Holder Name", "Full name on card", passengerName
        ));
        methodSpecs.put(PaymentMethod.E_WALLET, new MethodSpec(
                "WALLET // DIGITAL",
                "E-Wallet",
                "MoMo, ZaloPay, or VNPay",
                "Wallet ID / Phone", "0901111111", passengerPhone,
                "Wallet Provider", "MoMo", "MoMo"
        ));
        methodSpecs.put(PaymentMethod.BANK_TRANSFER, new MethodSpec(
                "BANK // WIRE",
                "Bank Transfer",
                "Direct interbank settlement",
                "Bank Name", "Vietcombank", "Vietcombank",
                "Account Number", "0123456789", "0123456789"
        ));
        methodSpecs.put(PaymentMethod.CASH, new MethodSpec(
                "COUNTER // CASH",
                "Counter Cash",
                "Settle at airport desk",
                "Counter Code", "CNT-01", "CNT-01",
                "Cashier ID", "CSH-01", "CSH-01"
        ));
    }

    private VBox buildHeroBanner() {
        Label eyebrow = ArbitrumTheme.createCyanEyebrowLabel("BOOKING SETTLEMENT // PAYMENT GATEWAY");
        Label title = ArbitrumTheme.createDisplayTitle("Settle Booking " + booking.getBookingId(), "display-md");
        Label subtitle = ArbitrumTheme.createHeroSubtitle(
                "Flight " + booking.getFlight().getFlightNumber()
                        + " (" + ReservationGuiController.formatRoute(booking.getFlight()) + ")"
                        + "  |  Seats: " + ReservationGuiController.formatSeatNumbers(booking)
                        + "  |  Status: " + booking.getStatus()
        );
        VBox banner = new VBox(ArbitrumTheme.SPACE_8, eyebrow, title, subtitle);
        banner.getStyleClass().add("hero-banner");
        return banner;
    }

    private VBox buildMethodSelectionCard() {
        VBox card = new VBox(ArbitrumTheme.SPACE_12);
        card.getStyleClass().add("card");

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("STEP 01 // PAYMENT METHOD");
        Label heading = ArbitrumTheme.createHeadingLabel("Choose Payment Rail", "heading-md");

        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(ArbitrumTheme.SPACE_12);
        cardsGrid.setVgap(ArbitrumTheme.SPACE_12);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            cardsGrid.getColumnConstraints().add(col);
        }

        PaymentMethod[] methods = PaymentMethod.values();
        for (int i = 0; i < methods.length; i++) {
            PaymentMethod method = methods[i];
            ToggleButton toggle = createMethodCardToggle(method);
            cardsGrid.add(toggle, i, 0);
            if (i == 0) {
                toggle.setSelected(true);
            }
        }

        methodGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) {
                    oldToggle.setSelected(true);
                }
                return;
            }
            PaymentMethod selected = (PaymentMethod) newToggle.getUserData();
            applyMethodFields(selected);
        });

        card.getChildren().addAll(eyebrow, heading, cardsGrid);
        return card;
    }

    private ToggleButton createMethodCardToggle(PaymentMethod method) {
        MethodSpec spec = methodSpecs.get(method);

        Label codeLabel = new Label(spec.code());
        codeLabel.getStyleClass().add("payment-card-code");

        Label titleLabel = new Label(spec.title());
        titleLabel.getStyleClass().add("payment-card-title");

        Label subtitleLabel = new Label(spec.subtitle());
        subtitleLabel.getStyleClass().add("payment-card-subtitle");
        subtitleLabel.setWrapText(true);

        VBox content = new VBox(4, codeLabel, titleLabel, subtitleLabel);
        content.setAlignment(Pos.CENTER_LEFT);

        ToggleButton button = new ToggleButton();
        button.setGraphic(content);
        button.setToggleGroup(methodGroup);
        button.setUserData(method);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(88);
        button.getStyleClass().add("payment-method-card");
        return button;
    }

    private VBox buildDetailsCard() {
        VBox card = new VBox(ArbitrumTheme.SPACE_12);
        card.getStyleClass().add("card-stone");

        Label eyebrow = ArbitrumTheme.createEyebrowLabel("STEP 02 // TRANSACTION CREDENTIALS");

        GridPane fieldsGrid = new GridPane();
        fieldsGrid.setHgap(ArbitrumTheme.SPACE_16);
        fieldsGrid.setVgap(ArbitrumTheme.SPACE_8);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            col.setHgrow(Priority.ALWAYS);
            fieldsGrid.getColumnConstraints().add(col);
        }

        ArbitrumTheme.styleTextInput(primaryField);
        ArbitrumTheme.styleTextInput(secondaryField);

        fieldsGrid.add(primaryFieldLabel, 0, 0);
        fieldsGrid.add(secondaryFieldLabel, 1, 0);
        fieldsGrid.add(primaryField, 0, 1);
        fieldsGrid.add(secondaryField, 1, 1);

        simulateFailureCheckBox.getStyleClass().add("payment-failure-toggle");

        attemptStatusLabel.managedProperty().bind(attemptStatusLabel.visibleProperty());
        attemptStatusLabel.setVisible(false);

        card.getChildren().addAll(eyebrow, fieldsGrid, simulateFailureCheckBox, attemptStatusLabel);
        return card;
    }

    private BorderPane buildFooterBar() {
        Label amountEyebrow = ArbitrumTheme.createEyebrowLabel("AMOUNT DUE");
        Label amountValue = ArbitrumTheme.createHeadingLabel(
                ReservationGuiController.formatCurrency(booking.getTotalPrice()),
                "heading-lg"
        );
        VBox summary = new VBox(2, amountEyebrow, amountValue);
        summary.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = ArbitrumTheme.createSecondaryButton("Cancel");
        cancelButton.setOnAction(e -> close());

        Button submitButton = ArbitrumTheme.createPrimaryButton("Submit Payment");
        submitButton.setOnAction(e -> submitPayment());

        HBox actions = new HBox(ArbitrumTheme.SPACE_12, cancelButton, submitButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane footer = new BorderPane();
        footer.getStyleClass().add("fare-summary-bar");
        footer.setLeft(summary);
        footer.setRight(actions);
        return footer;
    }

    private void applyMethodFields(PaymentMethod method) {
        MethodSpec spec = methodSpecs.get(method);
        primaryFieldLabel.setText(spec.primaryLabel());
        primaryField.setPromptText(spec.primaryPrompt());
        primaryField.setText(spec.primaryDefault());

        secondaryFieldLabel.setText(spec.secondaryLabel());
        secondaryField.setPromptText(spec.secondaryPrompt());
        secondaryField.setText(spec.secondaryDefault());
    }

    private void submitPayment() {
        PaymentMethod selectedMethod = (PaymentMethod) methodGroup.getSelectedToggle().getUserData();
        boolean simulateFailure = simulateFailureCheckBox.isSelected();

        try {
            Payment payment = controller.processPayment(
                    booking.getBookingId(),
                    selectedMethod,
                    primaryField.getText(),
                    secondaryField.getText(),
                    simulateFailure
            );
            if (onPaymentAttempted != null) {
                onPaymentAttempted.run();
            }

            if (payment.getStatus() == PaymentStatus.FAILED) {
                attemptStatusLabel.setText(
                        "Last attempt " + payment.getPaymentId()
                                + " FAILED — booking remains PENDING. Uncheck simulation to retry."
                );
                attemptStatusLabel.setVisible(true);
                ArbitrumTheme.showWarning(
                        this,
                        "Payment Failed (Simulated)",
                        "Payment " + payment.getPaymentId() + " (" + payment.getMethod() + ") failed.\n"
                                + "Booking " + booking.getBookingId() + " remains PENDING with seats reserved.\n"
                                + "Uncheck 'Simulate Payment Failure' and submit again to retry."
                );
                return;
            }

            ArbitrumTheme.showInfo(
                    this,
                    "Payment Confirmed",
                    "Payment " + payment.getPaymentId() + " completed (" + payment.getMethod() + ").\n"
                            + "Booking " + booking.getBookingId() + " is now " + booking.getStatus() + "."
            );
            close();
        } catch (AirlineReservationException ex) {
            ArbitrumTheme.showError(this, "Payment Failed", ex.getMessage());
        }
    }

    private record MethodSpec(String code,
                              String title,
                              String subtitle,
                              String primaryLabel,
                              String primaryPrompt,
                              String primaryDefault,
                              String secondaryLabel,
                              String secondaryPrompt,
                              String secondaryDefault) {
    }
}
