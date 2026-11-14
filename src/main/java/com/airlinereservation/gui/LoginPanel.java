package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Person;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * Login card supporting manual credential entry, one-click demo account quick-fill,
 * and launching passenger registration.
 */
public class LoginPanel extends JPanel {

    private static final String QUICK_FILL_PROMPT = "-- Quick Fill Demo Account --";

    private final ReservationGuiController controller;
    private final Consumer<Person> onLoginSuccess;
    private final Runnable onRegisterRequested;

    private final JComboBox<Object> quickFillCombo;
    private final JTextField identifierField;
    private final JPasswordField passwordField;

    public LoginPanel(ReservationGuiController controller,
                      Consumer<Person> onLoginSuccess,
                      Runnable onRegisterRequested) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
        this.onRegisterRequested = onRegisterRequested;

        setBackground(AnthropicTheme.PRIMARY_SURFACE);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        AnthropicTheme.styleCard(card);

        JLabel titleLabel = AnthropicTheme.createHeadingLabel("Sign In", 28);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitleLabel = AnthropicTheme.createMutedLabel(
                "<html><body style='width:360px'>Sign in with your Passenger ID / Email / Passport or Admin ID / Staff Code.</body></html>"
        );
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        quickFillCombo = new JComboBox<>();
        quickFillCombo.addItem(QUICK_FILL_PROMPT);
        for (DemoAccount account : controller.getDemoAccounts()) {
            quickFillCombo.addItem(account);
        }
        AnthropicTheme.styleComboBox(quickFillCombo);
        quickFillCombo.setAlignmentX(LEFT_ALIGNMENT);
        quickFillCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        quickFillCombo.addActionListener(e -> applyQuickFillSelection());

        identifierField = new JTextField();
        AnthropicTheme.styleTextInput(identifierField);
        identifierField.setAlignmentX(LEFT_ALIGNMENT);
        identifierField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        passwordField = new JPasswordField();
        AnthropicTheme.styleTextInput(passwordField);
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.addActionListener(e -> attemptLogin());

        JButton loginButton = new JButton("Login");
        AnthropicTheme.stylePrimaryButton(loginButton);
        loginButton.addActionListener(e -> attemptLogin());

        JButton registerButton = new JButton("Register Passenger");
        AnthropicTheme.styleSecondaryButton(registerButton);
        registerButton.addActionListener(e -> this.onRegisterRequested.run());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        buttonRow.setAlignmentX(LEFT_ALIGNMENT);
        buttonRow.add(loginButton);
        buttonRow.add(Box.createHorizontalStrut(AnthropicTheme.SPACE_12));
        buttonRow.add(registerButton);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_24));
        card.add(alignedLabel("Demo Quick Fill"));
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        card.add(quickFillCombo);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_16));
        card.add(alignedLabel("User ID / Email / Staff Code / Passport"));
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        card.add(identifierField);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_16));
        card.add(alignedLabel("Password"));
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(AnthropicTheme.SPACE_24));
        card.add(buttonRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(AnthropicTheme.SPACE_24, AnthropicTheme.SPACE_24, AnthropicTheme.SPACE_24, AnthropicTheme.SPACE_24);
        add(card, gbc);
    }

    public void clearInputs() {
        quickFillCombo.setSelectedIndex(0);
        identifierField.setText("");
        passwordField.setText("");
    }

    public void prefillIdentifier(String identifier) {
        identifierField.setText(identifier == null ? "" : identifier);
        passwordField.setText("");
        passwordField.requestFocusInWindow();
    }

    private void applyQuickFillSelection() {
        Object selected = quickFillCombo.getSelectedItem();
        if (selected instanceof DemoAccount account) {
            identifierField.setText(account.identifier());
            passwordField.setText(account.password());
        }
    }

    private void attemptLogin() {
        try {
            Person user = controller.login(
                    identifierField.getText(),
                    new String(passwordField.getPassword())
            );
            onLoginSuccess.accept(user);
        } catch (AirlineReservationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static JLabel alignedLabel(String text) {
        JLabel label = AnthropicTheme.createFieldLabel(text);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }
}
