package com.airlinereservation.gui;

import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Shared role-workspace shell providing a themed header banner and tab container.
 */
abstract class AbstractWorkspaceView extends BorderPane {

    protected final ReservationGuiController controller;
    protected final TabPane tabs = new TabPane();
    private final Label welcomeLabel;
    private final String defaultTitle;

    protected AbstractWorkspaceView(ReservationGuiController controller,
                                    String defaultTitle,
                                    String subtitle) {
        this.controller = controller;
        this.defaultTitle = defaultTitle;

        setPadding(new Insets(AnthropicTheme.SPACE_24));

        welcomeLabel = AnthropicTheme.createHeadingLabel(defaultTitle, "heading-lg");
        Label noteLabel = AnthropicTheme.createMutedLabel(subtitle);
        noteLabel.setWrapText(true);

        VBox banner = new VBox(AnthropicTheme.SPACE_8, welcomeLabel, noteLabel);
        banner.getStyleClass().add("subtle-panel");
        BorderPane.setMargin(banner, new Insets(0, 0, AnthropicTheme.SPACE_16, 0));

        tabs.getStyleClass().add("workspace-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        setTop(banner);
        setCenter(tabs);
    }

    public void refreshData() {
        Person user = controller.getCurrentUser();
        welcomeLabel.setText(user != null ? formatWelcomeTitle(user) : defaultTitle);
    }

    protected abstract String formatWelcomeTitle(Person user);

    protected static Tab createPlaceholderTab(String tabTitle, String heading, String description) {
        VBox card = new VBox(AnthropicTheme.SPACE_8,
                AnthropicTheme.createSectionLabel(heading),
                AnthropicTheme.createMutedLabel(description));
        card.getStyleClass().add("card");
        card.setMaxHeight(Double.MAX_VALUE);

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(AnthropicTheme.SPACE_8, 0, 0, 0));
        return createNonClosableTab(tabTitle, wrapper);
    }

    protected static Tab createNonClosableTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }
}
