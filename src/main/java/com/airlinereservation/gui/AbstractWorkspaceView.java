package com.airlinereservation.gui;

import com.airlinereservation.model.Person;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

/**
 * Shared role-workspace shell providing an Arbitrum Hero Gradient banner and segmented pill tabs.
 */
abstract class AbstractWorkspaceView extends BorderPane {

    protected final ReservationGuiController controller;
    protected final TabPane tabs = new TabPane();
    private final Label welcomeLabel;
    private final String defaultTitle;

    protected AbstractWorkspaceView(ReservationGuiController controller,
                                    String eyebrow,
                                    String defaultTitle,
                                    String subtitle) {
        this.controller = controller;
        this.defaultTitle = defaultTitle;

        setPadding(new Insets(ArbitrumTheme.SPACE_24, ArbitrumTheme.SPACE_32, ArbitrumTheme.SPACE_24, ArbitrumTheme.SPACE_32));

        Label eyebrowLabel = ArbitrumTheme.createCyanEyebrowLabel(eyebrow);
        welcomeLabel = ArbitrumTheme.createDisplayTitle(defaultTitle, "display-lg");
        welcomeLabel.setWrapText(true);

        Label noteLabel = ArbitrumTheme.createHeroSubtitle(subtitle);

        VBox banner = new VBox(ArbitrumTheme.SPACE_8, eyebrowLabel, welcomeLabel, noteLabel);
        banner.getStyleClass().add("hero-banner");
        BorderPane.setMargin(banner, new Insets(0, 0, ArbitrumTheme.SPACE_24, 0));

        tabs.getStyleClass().add("workspace-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        setTop(banner);
        setCenter(tabs);
    }

    public void refreshData() {
        Person user = controller.getCurrentUser();
        String title = user != null ? formatWelcomeTitle(user) : defaultTitle;
        welcomeLabel.setText(title.toUpperCase(Locale.ROOT));
    }

    protected abstract String formatWelcomeTitle(Person user);

    protected static Tab createPlaceholderTab(String tabTitle, String eyebrow, String heading, String description) {
        VBox card = new VBox(
                ArbitrumTheme.SPACE_8,
                ArbitrumTheme.createEyebrowLabel(eyebrow),
                ArbitrumTheme.createHeadingLabel(heading, "heading-md"),
                ArbitrumTheme.createMutedLabel(description)
        );
        card.getStyleClass().add("card");
        card.setMaxHeight(Double.MAX_VALUE);

        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(ArbitrumTheme.SPACE_8, 0, 0, 0));
        return createNonClosableTab(tabTitle, wrapper);
    }

    protected static Tab createNonClosableTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }
}
