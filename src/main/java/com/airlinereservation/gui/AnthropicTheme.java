package com.airlinereservation.gui;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Centralized JavaFX theme helper and dialog utilities aligned with {@code DESIGN.md}.
 */
public final class AnthropicTheme {

    public static final String STYLESHEET = Objects.requireNonNull(
            AnthropicTheme.class.getResource("anthropic-theme.css")
    ).toExternalForm();

    public static final double SPACE_8 = 8.0;
    public static final double SPACE_12 = 12.0;
    public static final double SPACE_16 = 16.0;
    public static final double SPACE_24 = 24.0;

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private AnthropicTheme() {
    }

    public static void applyStylesheet(Scene scene) {
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }

    public static Label createHeadingLabel(String text, String sizeStyleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("heading-serif", sizeStyleClass);
        return label;
    }

    public static Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-label");
        return label;
    }

    public static Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    public static Label createMutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-label");
        return label;
    }

    public static Label createBadgeLabel(String text, String badgeStyleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", badgeStyleClass);
        return badge;
    }

    public static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-primary");
        return button;
    }

    public static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("btn-secondary");
        return button;
    }

    public static void styleTextInput(TextInputControl input) {
        input.getStyleClass().add("input-field");
        input.setPrefHeight(40);
    }

    public static void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setPrefHeight(40);
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    public static DatePicker createIsoDatePicker(LocalDate initialDate) {
        DatePicker picker = new DatePicker(initialDate);
        picker.setPrefHeight(40);
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setPromptText("YYYY-MM-DD");
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : ISO_DATE.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text.trim(), ISO_DATE);
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        });
        return picker;
    }

    public static void showInfo(Window owner, String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, owner, title, message);
    }

    public static void showWarning(Window owner, String title, String message) {
        showAlert(Alert.AlertType.WARNING, owner, title, message);
    }

    public static void showError(Window owner, String title, String message) {
        showAlert(Alert.AlertType.ERROR, owner, title, message);
    }

    private static void showAlert(Alert.AlertType type, Window owner, String title, String message) {
        Alert alert = new Alert(type);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(STYLESHEET);
        pane.setMinWidth(Control.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
