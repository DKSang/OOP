package com.airlinereservation.gui;

import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Top-level Swing window managing menu actions, session header, and role-based workspace routing.
 */
public class MainFrame extends JFrame {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_PASSENGER = "PASSENGER";
    private static final String CARD_ADMIN = "ADMIN";

    private final ReservationGuiController controller;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardContainer = new JPanel(cardLayout);

    private final LoginPanel loginPanel;
    private final PassengerWorkspacePanel passengerWorkspacePanel;
    private final AdminWorkspacePanel adminWorkspacePanel;

    private final JLabel userStatusLabel;
    private final JLabel roleBadgeLabel;
    private final JButton headerLogoutButton;
    private final JMenuItem logoutMenuItem;

    public MainFrame(ReservationGuiController controller) {
        super("Airline Reservation System");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1120, 760);
        getContentPane().setBackground(AnthropicTheme.PRIMARY_SURFACE);
        setLayout(new BorderLayout());

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AnthropicTheme.PRIMARY_BORDER));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(AnthropicTheme.sansFont(java.awt.Font.PLAIN, 14));

        JMenuItem resetDemoItem = new JMenuItem("Reset Demo Data");
        resetDemoItem.addActionListener(e -> handleResetDemoData());

        logoutMenuItem = new JMenuItem("Logout");
        logoutMenuItem.addActionListener(e -> handleLogout());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> dispose());

        fileMenu.add(resetDemoItem);
        fileMenu.add(logoutMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Header bar
        JPanel headerBar = new JPanel(new BorderLayout(AnthropicTheme.SPACE_16, 0));
        headerBar.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AnthropicTheme.PRIMARY_BORDER),
                AnthropicTheme.spacing(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_24)
        ));

        JLabel brandLabel = AnthropicTheme.createHeadingLabel("Airline Reservation System", 22);
        headerBar.add(brandLabel, BorderLayout.WEST);

        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, AnthropicTheme.SPACE_12, 0));
        sessionPanel.setBackground(AnthropicTheme.PRIMARY_SURFACE);

        userStatusLabel = AnthropicTheme.createMutedLabel("Not signed in");
        roleBadgeLabel = AnthropicTheme.createBadgeLabel("Guest", AnthropicTheme.TERTIARY_SURFACE, AnthropicTheme.PRIMARY_TEXT);

        headerLogoutButton = new JButton("Logout");
        AnthropicTheme.styleSecondaryButton(headerLogoutButton);
        headerLogoutButton.addActionListener(e -> handleLogout());

        sessionPanel.add(userStatusLabel);
        sessionPanel.add(roleBadgeLabel);
        sessionPanel.add(headerLogoutButton);
        headerBar.add(sessionPanel, BorderLayout.EAST);

        add(headerBar, BorderLayout.NORTH);

        // Card container
        cardContainer.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        loginPanel = new LoginPanel(controller, this::handleLoginSuccess, this::openRegistrationDialog);
        passengerWorkspacePanel = new PassengerWorkspacePanel(controller);
        adminWorkspacePanel = new AdminWorkspacePanel(controller);

        cardContainer.add(loginPanel, CARD_LOGIN);
        cardContainer.add(passengerWorkspacePanel, CARD_PASSENGER);
        cardContainer.add(adminWorkspacePanel, CARD_ADMIN);
        add(cardContainer, BorderLayout.CENTER);

        updateSessionHeader();
        cardLayout.show(cardContainer, CARD_LOGIN);
        setLocationRelativeTo(null);
    }

    private void handleLoginSuccess(Person user) {
        updateSessionHeader();
        if (controller.isPassengerLoggedIn()) {
            passengerWorkspacePanel.refreshData();
            cardLayout.show(cardContainer, CARD_PASSENGER);
        } else if (controller.isAdminLoggedIn()) {
            adminWorkspacePanel.refreshData();
            cardLayout.show(cardContainer, CARD_ADMIN);
        }
    }

    private void openRegistrationDialog() {
        PassengerRegistrationDialog dialog = new PassengerRegistrationDialog(this, controller);
        dialog.setVisible(true);
        Passenger registered = dialog.getRegisteredPassenger();
        if (registered != null) {
            loginPanel.prefillIdentifier(registered.getId());
        }
    }

    private void handleLogout() {
        controller.logout();
        loginPanel.clearInputs();
        updateSessionHeader();
        cardLayout.show(cardContainer, CARD_LOGIN);
    }

    private void handleResetDemoData() {
        controller.resetDemoData();
        loginPanel.clearInputs();
        updateSessionHeader();
        cardLayout.show(cardContainer, CARD_LOGIN);
        JOptionPane.showMessageDialog(
                this,
                "Demo data has been restored to its initial state.",
                "Demo Reset",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateSessionHeader() {
        Person currentUser = controller.getCurrentUser();
        if (currentUser == null) {
            userStatusLabel.setText("Not signed in");
            roleBadgeLabel.setText("Guest");
            roleBadgeLabel.setBackground(AnthropicTheme.TERTIARY_SURFACE);
            roleBadgeLabel.setForeground(AnthropicTheme.PRIMARY_TEXT);
            headerLogoutButton.setVisible(false);
            logoutMenuItem.setEnabled(false);
            return;
        }

        userStatusLabel.setText(currentUser.getFullName() + " (" + currentUser.getId() + ")");
        roleBadgeLabel.setText(currentUser.getRole());
        if (controller.isAdminLoggedIn()) {
            roleBadgeLabel.setBackground(AnthropicTheme.COOL_ACCENT);
            roleBadgeLabel.setForeground(AnthropicTheme.PRIMARY_CTA_TEXT);
        } else {
            roleBadgeLabel.setBackground(AnthropicTheme.SAGE_ACCENT);
            roleBadgeLabel.setForeground(AnthropicTheme.PRIMARY_CTA_TEXT);
        }
        headerLogoutButton.setVisible(true);
        logoutMenuItem.setEnabled(true);
    }
}
