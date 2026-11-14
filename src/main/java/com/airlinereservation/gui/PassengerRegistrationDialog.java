package com.airlinereservation.gui;

import com.airlinereservation.exception.AirlineReservationException;
import com.airlinereservation.model.Passenger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Modal dialog allowing a new Passenger to register an account directly from the login screen.
 */
public class PassengerRegistrationDialog extends JDialog {

    private final ReservationGuiController controller;
    private Passenger registeredPassenger;

    private final JTextField idField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField dobField = new JTextField("2000-01-01");
    private final JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JTextField passportField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public PassengerRegistrationDialog(JFrame owner, ReservationGuiController controller) {
        super(owner, "Register New Passenger", true);
        this.controller = controller;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AnthropicTheme.PRIMARY_SURFACE);
        setLayout(new BorderLayout(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_16));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        content.setBorder(AnthropicTheme.spacing(AnthropicTheme.SPACE_24, AnthropicTheme.SPACE_24));

        content.add(AnthropicTheme.createHeadingLabel("Passenger Registration", 24));
        content.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        content.add(AnthropicTheme.createMutedLabel("Create a passenger profile to search flights and manage bookings."));
        content.add(Box.createVerticalStrut(AnthropicTheme.SPACE_16));

        JPanel formGrid = new JPanel(new GridLayout(0, 2, AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_12));
        formGrid.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        AnthropicTheme.styleTextInput(idField);
        AnthropicTheme.styleTextInput(fullNameField);
        AnthropicTheme.styleTextInput(emailField);
        AnthropicTheme.styleTextInput(phoneField);
        AnthropicTheme.styleTextInput(dobField);
        AnthropicTheme.styleComboBox(genderCombo);
        AnthropicTheme.styleTextInput(passportField);
        AnthropicTheme.styleTextInput(passwordField);

        addRow(formGrid, "Passenger ID (e.g. PAX-03)", idField);
        addRow(formGrid, "Full Name", fullNameField);
        addRow(formGrid, "Email", emailField);
        addRow(formGrid, "Phone Number", phoneField);
        addRow(formGrid, "Date of Birth (YYYY-MM-DD)", dobField);
        addRow(formGrid, "Gender", genderCombo);
        addRow(formGrid, "Passport / CCCD", passportField);
        addRow(formGrid, "Password", passwordField);

        content.add(formGrid);
        content.add(Box.createVerticalStrut(AnthropicTheme.SPACE_24));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AnthropicTheme.SPACE_12, 0));
        actions.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        JButton cancelButton = new JButton("Cancel");
        AnthropicTheme.styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());

        JButton submitButton = new JButton("Register Passenger");
        AnthropicTheme.stylePrimaryButton(submitButton);
        submitButton.addActionListener(e -> submitRegistration());

        actions.add(cancelButton);
        actions.add(submitButton);
        content.add(actions);

        add(content, BorderLayout.CENTER);
        pack();
        setMinimumSize(new java.awt.Dimension(540, getPreferredSize().height));
        setLocationRelativeTo(owner);
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
                    dobField.getText(),
                    String.valueOf(genderCombo.getSelectedItem()),
                    passportField.getText(),
                    new String(passwordField.getPassword())
            );
            JOptionPane.showMessageDialog(
                    this,
                    "Registered passenger " + registeredPassenger.getFullName()
                            + " (" + registeredPassenger.getId() + "). You can now log in.",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (AirlineReservationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void addRow(JPanel grid, String labelText, java.awt.Component input) {
        grid.add(AnthropicTheme.createFieldLabel(labelText));
        grid.add(input);
    }
}
