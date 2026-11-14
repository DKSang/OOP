package com.airlinereservation.gui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Centralized JavaFX Web3 SPA theme engine, bundled Arbitrum typography loader,
 * and backdrop-blurred modal helpers aligned with {@code DESIGN.md}.
 */
public final class ArbitrumTheme {

    public static final String STYLESHEET_RESOURCE = "arbitrum-theme.css";

    public static final String STYLESHEET = Objects.requireNonNull(
            ArbitrumTheme.class.getResource(STYLESHEET_RESOURCE)
    ).toExternalForm();

    public static final List<String> FONT_RESOURCES = List.of(
            "fonts/Aeonik-Regular.otf",
            "fonts/Aeonik-Medium.otf",
            "fonts/FKScreamer-Upright.otf",
            "fonts/DMMono-Regular.ttf"
    );

    public static final double CONTENT_MAX_WIDTH = 1440.0;
    public static final double SPACE_8 = 8.0;
    public static final double SPACE_12 = 12.0;
    public static final double SPACE_16 = 16.0;
    public static final double SPACE_24 = 24.0;
    public static final double SPACE_32 = 32.0;

    private static final String COLOR_ARBITRUM_BLUE = "#016BE5";

    private static final String ARBITRUM_HEX_SVG =
            "M144.518 39.2113L82.214 3.03438C80.1932 1.87961 77.9919 1.28418 75.7545 1.28418C73.5352 1.28418 "
                    + "71.3158 1.87961 69.3311 3.03438L7.00937 39.2474C3.05788 41.5569 0.585938 45.8151 "
                    + "0.585938 50.4162V122.842C0.585938 127.443 3.05788 131.756 7.00937 134.047L18.2143 140.561"
                    + "L34.5616 150.052L38.2243 139.965L71.8932 47.1143C72.2 46.2121 71.5504 45.2378 70.54 45.2378"
                    + "H54.752C53.5792 45.2378 52.4966 45.9775 52.1177 47.0782L21.8951 130.474L12.2961 124.899"
                    + "C11.5743 124.484 11.1232 123.708 11.1232 122.878V50.4162C11.1232 49.5862 11.5743 48.8103 "
                    + "12.2961 48.3953L74.6178 12.1824C74.9606 11.9658 75.3756 11.8756 75.7725 11.8756C76.1875 "
                    + "11.8756 76.5665 11.9839 76.9454 12.1824L139.285 48.3953C140.007 48.8103 140.458 49.5862 "
                    + "140.458 50.4162V122.842C140.458 123.672 140.007 124.448 139.285 124.863L129.794 130.384"
                    + "L104.822 61.4768C104.371 60.2137 102.639 60.2137 102.188 61.4768L93.9602 84.1392"
                    + "C93.7257 84.7707 93.7257 85.4744 93.9602 86.0879L113.447 139.875L106.969 143.646"
                    + "L91.0192 99.6023C90.5681 98.3393 88.8359 98.3393 88.3848 99.6023L80.1571 122.265"
                    + "C79.9225 122.896 79.9225 123.6 80.1571 124.213L90.6403 153.137L76.9634 161.094"
                    + "C76.6206 161.311 76.2056 161.401 75.7906 161.401C75.3756 161.401 74.9967 161.293 "
                    + "74.6178 161.094L61.1033 153.227L99.5537 47.1323C99.8965 46.1941 99.2108 45.2017 "
                    + "98.2365 45.2017H82.4486C81.2758 45.2017 80.1932 45.9414 79.8142 47.0421L44.756 143.718"
                    + "L41.0932 153.805L57.4405 163.295L69.3672 170.224C71.3519 171.379 73.5713 171.974 "
                    + "75.7906 171.974C78.0099 171.974 80.2292 171.379 82.214 170.224L144.554 134.011"
                    + "C148.541 131.701 150.977 127.443 150.977 122.806V50.4162C150.977 45.8151 148.505 "
                    + "41.5028 144.554 39.2113H144.518Z";

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static boolean fontsLoaded;

    private ArbitrumTheme() {
    }

    /**
     * Registers all bundled Arbitrum font faces ({@code Aeonik}, {@code FK Screamer},
     * and {@code DM Mono}) with the JavaFX font registry once per JVM session.
     */
    public static synchronized void loadFonts() {
        if (fontsLoaded) {
            return;
        }
        for (String resourcePath : FONT_RESOURCES) {
            try (InputStream stream = ArbitrumTheme.class.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    throw new IllegalStateException("Missing bundled font resource: " + resourcePath);
                }
                if (Font.loadFont(stream, 14) == null) {
                    throw new IllegalStateException("Failed to decode bundled font resource: " + resourcePath);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load bundled font resource: " + resourcePath, ex);
            }
        }
        fontsLoaded = true;
    }

    public static void applyStylesheet(Scene scene) {
        loadFonts();
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }

    public static Label createDisplayTitle(String text, String sizeStyleClass) {
        return createUppercaseLabel(text, "display-screamer", sizeStyleClass);
    }

    public static Label createHeadingLabel(String text, String sizeStyleClass) {
        return createLabel(text, "heading-aeonik", sizeStyleClass);
    }

    public static Label createEyebrowLabel(String text) {
        return createUppercaseLabel(text, "eyebrow-mono");
    }

    public static Label createCyanEyebrowLabel(String text) {
        return createUppercaseLabel(text, "eyebrow-mono-cyan");
    }

    public static Label createHeroSubtitle(String text) {
        Label label = createLabel(text, "hero-subtitle");
        label.setWrapText(true);
        return label;
    }

    public static Label createFieldLabel(String text) {
        return createLabel(text, "field-label");
    }

    public static Label createMutedLabel(String text) {
        return createLabel(text, "muted-label");
    }

    public static Label createMonoLabel(String text) {
        return createLabel(text, "mono-value");
    }

    public static Label createBadgeLabel(String text, String badgeStyleClass) {
        return createLabel(text, "badge", badgeStyleClass);
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

    public static Button createNavPillButton(String text, boolean primary) {
        Button button = primary ? createPrimaryButton(text) : createSecondaryButton(text);
        button.getStyleClass().add("btn-nav-pill");
        return button;
    }

    public static Node createBrandMark(double targetHeight) {
        SVGPath mark = new SVGPath();
        mark.setContent(ARBITRUM_HEX_SVG);
        mark.setFill(Color.web(COLOR_ARBITRUM_BLUE));
        double scale = targetHeight / 172.0;
        mark.setScaleX(scale);
        mark.setScaleY(scale);
        Group scaledGroup = new Group(mark);
        StackPane container = new StackPane(scaledGroup);
        double targetWidth = 151.0 * scale;
        container.setPrefSize(targetWidth, targetHeight);
        container.setMinSize(targetWidth, targetHeight);
        container.setMaxSize(targetWidth, targetHeight);
        return container;
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

    public static void showModalWithBackdrop(Window owner, Stage dialog) {
        runWithBackdropBlur(owner, dialog::showAndWait);
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

    public static boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = buildStyledAlert(Alert.AlertType.CONFIRMATION, owner, title, message);
        boolean[] confirmed = {false};
        runWithBackdropBlur(owner, () -> alert.showAndWait()
                .filter(button -> button == ButtonType.OK)
                .ifPresent(button -> confirmed[0] = true));
        return confirmed[0];
    }

    private static Label createLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private static Label createUppercaseLabel(String text, String... styleClasses) {
        return createLabel(text == null ? "" : text.toUpperCase(Locale.ROOT), styleClasses);
    }

    private static void showAlert(Alert.AlertType type, Window owner, String title, String message) {
        Alert alert = buildStyledAlert(type, owner, title, message);
        runWithBackdropBlur(owner, alert::showAndWait);
    }

    private static Alert buildStyledAlert(Alert.AlertType type, Window owner, String title, String message) {
        loadFonts();
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
        return alert;
    }

    private static void runWithBackdropBlur(Window owner, Runnable modalAction) {
        Node ownerRoot = (owner != null && owner.getScene() != null) ? owner.getScene().getRoot() : null;
        if (ownerRoot == null) {
            modalAction.run();
            return;
        }
        Effect previousEffect = ownerRoot.getEffect();
        ColorAdjust dimEffect = new ColorAdjust(0.0, 0.0, -0.08, 0.0);
        dimEffect.setInput(new GaussianBlur(8.0));
        ownerRoot.setEffect(dimEffect);
        try {
            modalAction.run();
        } finally {
            ownerRoot.setEffect(previousEffect);
        }
    }
}
