package com.airlinereservation.gui;

import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Top-level Web3 SPA JavaFX stage coordinator managing the sticky DApp navbar,
 * architectural grid shell, and role-based workspace routing.
 */
public class MainWindow {

    private final Stage stage;
    private final ReservationGuiController controller;

    private final StackPane contentContainer = new StackPane();
    private final LoginView loginView;
    private final PassengerWorkspaceView passengerWorkspaceView;
    private final AdminWorkspaceView adminWorkspaceView;

    private final Label workspaceContextLabel = new Label("GUEST PORTAL");
    private final Label sessionBadgeLabel = ArbitrumTheme.createBadgeLabel("Guest · Not Signed In", "badge-guest");
    private final Button resetDemoButton = ArbitrumTheme.createNavPillButton("Reset Demo", false);
    private final Button headerLogoutButton = ArbitrumTheme.createNavPillButton("Logout", true);

    public MainWindow(Stage stage, ReservationGuiController controller) {
        this.stage = stage;
        this.controller = controller;

        stage.setTitle("Arbitrum Air — Reservation System");
        stage.setMinWidth(1040);
        stage.setMinHeight(740);

        HBox navbar = buildWeb3Navbar();

        loginView = new LoginView(controller, this::handleLoginSuccess, this::openRegistrationDialog);
        passengerWorkspaceView = new PassengerWorkspaceView(controller);
        adminWorkspaceView = new AdminWorkspaceView(controller);

        contentContainer.setMaxWidth(ArbitrumTheme.CONTENT_MAX_WIDTH);
        contentContainer.getStyleClass().add("bg-repeating-lines");
        contentContainer.getChildren().setAll(loginView);

        StackPane pageFrame = new StackPane(contentContainer);
        pageFrame.setAlignment(Pos.TOP_CENTER);
        pageFrame.getStyleClass().add("page-frame");

        ScrollPane pageScroller = new ScrollPane(pageFrame);
        pageScroller.setFitToWidth(true);
        pageScroller.setFitToHeight(true);
        pageScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pageScroller.getStyleClass().add("page-scroller");

        BorderPane root = new BorderPane();
        root.setTop(navbar);
        root.setCenter(pageScroller);

        updateSessionHeader();

        Scene scene = new Scene(root, 1240, 820);
        ArbitrumTheme.applyStylesheet(scene);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public void show() {
        stage.show();
    }

    private HBox buildWeb3Navbar() {
        HBox navbar = new HBox(ArbitrumTheme.SPACE_16);
        navbar.getStyleClass().add("web3-navbar");
        navbar.setAlignment(Pos.CENTER_LEFT);

        Node brandMark = ArbitrumTheme.createBrandMark(28);
        Label wordmark = new Label("ARBITRUM AIR");
        wordmark.getStyleClass().add("navbar-wordmark");

        Region divider = new Region();
        divider.getStyleClass().add("navbar-divider");

        workspaceContextLabel.getStyleClass().add("navbar-context");

        HBox brandCluster = new HBox(ArbitrumTheme.SPACE_12, brandMark, wordmark, divider, workspaceContextLabel);
        brandCluster.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resetDemoButton.setOnAction(e -> handleResetDemoData());

        headerLogoutButton.setOnAction(e -> handleLogout());
        headerLogoutButton.managedProperty().bind(headerLogoutButton.visibleProperty());

        HBox sessionBar = new HBox(
                ArbitrumTheme.SPACE_12,
                sessionBadgeLabel,
                resetDemoButton,
                headerLogoutButton
        );
        sessionBar.setAlignment(Pos.CENTER_RIGHT);

        navbar.getChildren().addAll(brandCluster, spacer, sessionBar);
        return navbar;
    }

    private void handleLoginSuccess(Person user) {
        updateSessionHeader();
        if (controller.isPassengerLoggedIn()) {
            passengerWorkspaceView.refreshData();
            switchContent(passengerWorkspaceView);
        } else if (controller.isAdminLoggedIn()) {
            adminWorkspaceView.refreshData();
            switchContent(adminWorkspaceView);
        }
    }

    private void openRegistrationDialog() {
        PassengerRegistrationDialog dialog = new PassengerRegistrationDialog(stage, controller);
        ArbitrumTheme.showModalWithBackdrop(stage, dialog);
        Passenger registered = dialog.getRegisteredPassenger();
        if (registered != null) {
            loginView.prefillIdentifier(registered.getId());
        }
    }

    private void handleLogout() {
        controller.logout();
        returnToLogin();
    }

    private void handleResetDemoData() {
        controller.resetDemoData();
        returnToLogin();
        ArbitrumTheme.showInfo(
                stage,
                "Demo Reset",
                "Demo data has been restored to its initial state."
        );
    }

    private void returnToLogin() {
        loginView.clearInputs();
        loginView.refreshMetrics();
        updateSessionHeader();
        switchContent(loginView);
    }

    private void switchContent(Node view) {
        contentContainer.getChildren().setAll(view);
    }

    private void updateSessionHeader() {
        Person currentUser = controller.getCurrentUser();
        sessionBadgeLabel.getStyleClass().removeAll("badge-guest", "badge-passenger", "badge-admin");

        if (currentUser == null) {
            workspaceContextLabel.setText("GUEST PORTAL");
            sessionBadgeLabel.setText("Guest · Not Signed In");
            sessionBadgeLabel.getStyleClass().add("badge-guest");
            headerLogoutButton.setVisible(false);
            return;
        }

        boolean isAdmin = controller.isAdminLoggedIn();
        workspaceContextLabel.setText(isAdmin ? "ADMIN CONSOLE" : "PASSENGER WORKSPACE");
        sessionBadgeLabel.setText(currentUser.getId() + " · " + currentUser.getRole());
        sessionBadgeLabel.getStyleClass().add(isAdmin ? "badge-admin" : "badge-passenger");
        headerLogoutButton.setVisible(true);
    }
}
