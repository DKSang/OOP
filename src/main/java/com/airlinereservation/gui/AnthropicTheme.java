package com.airlinereservation.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized visual design tokens and Swing styling helpers derived from {@code DESIGN.md}.
 */
public final class AnthropicTheme {

    public static final Color PRIMARY_SURFACE = Color.decode("#FAF9F5");
    public static final Color TERTIARY_SURFACE = Color.decode("#E3DACC");
    public static final Color PRIMARY_TEXT = Color.decode("#141413");
    public static final Color PRIMARY_CTA_BG = PRIMARY_TEXT;
    public static final Color PRIMARY_CTA_TEXT = PRIMARY_SURFACE;
    public static final Color WARM_ACCENT = Color.decode("#D97757");
    public static final Color EARTH_ACCENT = Color.decode("#C6613F");
    public static final Color SAGE_ACCENT = Color.decode("#788C5D");
    public static final Color COOL_ACCENT = Color.decode("#6A9BCC");
    public static final Color SOFT_BLUSH = Color.decode("#EBCECE");
    public static final Color SOFT_SEAFOAM = Color.decode("#BCD1CA");
    public static final Color PRIMARY_BORDER = Color.decode("#C6C4BA");
    public static final Color SECONDARY_BORDER = Color.decode("#B0AEA5");
    public static final Color NEUTRAL_DARK = Color.decode("#73726C");
    public static final Color NEUTRAL_MUTED = Color.decode("#87867F");

    public static final int SPACE_8 = 8;
    public static final int SPACE_12 = 12;
    public static final int SPACE_16 = 16;
    public static final int SPACE_24 = 24;
    public static final int SPACE_32 = 32;
    public static final int SPACE_48 = 48;

    private static final Set<String> AVAILABLE_FONTS = Arrays.stream(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
    ).collect(Collectors.toUnmodifiableSet());

    public static final String SERIF_FAMILY = resolveFontFamily(Font.SERIF, "Anthropic Serif", "Georgia", "Times New Roman");
    public static final String SANS_FAMILY = resolveFontFamily(Font.SANS_SERIF, "Anthropic Sans", "Segoe UI", "Roboto", "Helvetica", "Arial");

    private AnthropicTheme() {
    }

    public static Font serifFont(int style, int size) {
        return new Font(SERIF_FAMILY, style, size);
    }

    public static Font sansFont(int style, int size) {
        return new Font(SANS_FAMILY, style, size);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BORDER, 1),
                BorderFactory.createEmptyBorder(SPACE_24, SPACE_24, SPACE_24, SPACE_24)
        );
    }

    public static Border subtleSectionBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_BORDER, 1),
                BorderFactory.createEmptyBorder(SPACE_16, SPACE_24, SPACE_16, SPACE_24)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BORDER, 1),
                BorderFactory.createEmptyBorder(SPACE_8, SPACE_12, SPACE_8, SPACE_12)
        );
    }

    public static Border focusedInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARM_ACCENT, 1),
                BorderFactory.createEmptyBorder(SPACE_8, SPACE_12, SPACE_8, SPACE_12)
        );
    }

    public static Border spacing(int vertical, int horizontal) {
        return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(PRIMARY_SURFACE);
        panel.setBorder(cardBorder());
    }

    public static void styleSubtlePanel(JPanel panel) {
        panel.setBackground(TERTIARY_SURFACE);
        panel.setBorder(subtleSectionBorder());
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFont(sansFont(Font.PLAIN, 16));
        button.setBackground(PRIMARY_CTA_BG);
        button.setForeground(PRIMARY_CTA_TEXT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_CTA_BG, 1),
                BorderFactory.createEmptyBorder(SPACE_8, SPACE_16, SPACE_8, SPACE_16)
        ));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(serifFont(Font.PLAIN, 20));
        button.setForeground(PRIMARY_TEXT);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(SPACE_8, SPACE_16, SPACE_8, SPACE_16));
    }

    public static void styleTextInput(JTextComponent field) {
        field.setFont(sansFont(Font.PLAIN, 17));
        field.setBackground(PRIMARY_SURFACE);
        field.setForeground(PRIMARY_TEXT);
        field.setCaretColor(PRIMARY_TEXT);
        field.setBorder(inputBorder());
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(focusedInputBorder());
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(inputBorder());
            }
        });
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(sansFont(Font.PLAIN, 14));
        comboBox.setBackground(PRIMARY_SURFACE);
        comboBox.setForeground(PRIMARY_TEXT);
        comboBox.setBorder(BorderFactory.createLineBorder(PRIMARY_BORDER, 1));
    }

    public static JLabel createHeadingLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(serifFont(Font.PLAIN, size));
        label.setForeground(PRIMARY_TEXT);
        return label;
    }

    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(sansFont(Font.BOLD, 16));
        label.setForeground(PRIMARY_TEXT);
        return label;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(sansFont(Font.PLAIN, 12));
        label.setForeground(PRIMARY_TEXT);
        return label;
    }

    public static JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(sansFont(Font.PLAIN, 13));
        label.setForeground(NEUTRAL_DARK);
        return label;
    }

    public static JLabel createBadgeLabel(String text, Color background, Color foreground) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(background);
        badge.setForeground(foreground);
        badge.setFont(sansFont(Font.BOLD, 12));
        badge.setBorder(BorderFactory.createEmptyBorder(4, SPACE_12, 4, SPACE_12));
        return badge;
    }

    public static JLabel createOutlinedBadgeLabel(String text, Color background, Color foreground, Color borderColor) {
        JLabel badge = createBadgeLabel(text, background, foreground);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(4, SPACE_12, 4, SPACE_12)
        ));
        return badge;
    }

    private static String resolveFontFamily(String fallback, String... preferred) {
        for (String candidate : preferred) {
            if (AVAILABLE_FONTS.contains(candidate)) {
                return candidate;
            }
        }
        return fallback;
    }
}
