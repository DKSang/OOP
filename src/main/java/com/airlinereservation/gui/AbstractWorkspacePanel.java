package com.airlinereservation.gui;

import com.airlinereservation.model.Person;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Shared role-workspace shell providing a themed header banner and tabbed container.
 */
abstract class AbstractWorkspacePanel extends JPanel {

    protected final ReservationGuiController controller;
    protected final JTabbedPane tabs;
    private final JLabel welcomeLabel;
    private final String defaultTitle;

    protected AbstractWorkspacePanel(ReservationGuiController controller,
                                     String defaultTitle,
                                     String subtitle) {
        this.controller = controller;
        this.defaultTitle = defaultTitle;

        setBackground(AnthropicTheme.PRIMARY_SURFACE);
        setLayout(new BorderLayout(AnthropicTheme.SPACE_16, AnthropicTheme.SPACE_16));
        setBorder(AnthropicTheme.spacing(AnthropicTheme.SPACE_24, AnthropicTheme.SPACE_24));

        JPanel banner = new JPanel(new GridLayout(0, 1, 0, AnthropicTheme.SPACE_8));
        AnthropicTheme.styleSubtlePanel(banner);

        welcomeLabel = AnthropicTheme.createHeadingLabel(defaultTitle, 22);
        JLabel noteLabel = AnthropicTheme.createMutedLabel(subtitle);
        banner.add(welcomeLabel);
        banner.add(noteLabel);

        tabs = new JTabbedPane();
        tabs.setFont(AnthropicTheme.sansFont(Font.PLAIN, 14));
        tabs.setBackground(AnthropicTheme.PRIMARY_SURFACE);
        tabs.setForeground(AnthropicTheme.PRIMARY_TEXT);

        add(banner, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public void refreshData() {
        Person user = controller.getCurrentUser();
        welcomeLabel.setText(user != null ? formatWelcomeTitle(user) : defaultTitle);
    }

    protected abstract String formatWelcomeTitle(Person user);

    protected static JPanel createPlaceholderTab(String title, String description) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        AnthropicTheme.styleCard(panel);
        panel.add(AnthropicTheme.createSectionLabel(title));
        panel.add(Box.createVerticalStrut(AnthropicTheme.SPACE_8));
        panel.add(AnthropicTheme.createMutedLabel(description));
        return panel;
    }
}
