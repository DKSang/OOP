package com.airlinereservation.gui;

import com.airlinereservation.model.Passenger;
import com.airlinereservation.model.Person;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Top-level JavaFX stage coordinator managing menu actions, session header,
 * and role-based workspace routing.
 */
public class MainWindow {

    private final Stage stage;
    private final ReservationGuiController controller;

    private final StackPane contentContainer = new StackPane();
    private final LoginView loginView;
    private final PassengerWorkspaceView passengerWorkspaceView;
    private final AdminWorkspaceView adminWorkspaceView;

    private final Label userStatusLabel = AnthropicTheme.createMutedLabel("Not signed in");
    private final Label roleBadgeLabel = AnthropicTheme.createBadgeLabel("Guest", "badge-guest");
    private final Button headerLogoutButton = AnthropicTheme.createSecondaryButton("Logout");
    private final MenuItem logoutMenuItem = new MenuItem("Logout");

    public MainWindow(Stage stage, ReservationGuiController controller) {
        this.stage = stage;
        this.controller = controller;

        stage.setTitle("Airline Reservation System");
        stage.setMinWidth(1020);
        stage.setMinHeight(720);

        MenuBar menuBar = buildMenuBar();
        HBox headerBar = buildHeaderBar();

        VBox topSection = new VBox(menuBar, headerBar);

        loginView = new LoginView(controller, this::handleLoginSuccess, this::openRegistrationDialog);
        passengerWorkspaceView = new PassengerWorkspaceView(controller);
        adminWorkspaceView = new AdminWorkspaceView(controller);
        contentContainer.getChildren().setAll(loginView);

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(contentContainer);

        updateSessionHeader();

        Scene scene = new Scene(root, 1140, 780);
        AnthropicTheme.applyStylesheet(scene);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public void show() {
        stage.show();
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("app-menu-bar");

        Menu fileMenu = new Menu("File");

        MenuItem resetDemoItem = new MenuItem("Reset Demo Data");
        resetDemoItem.setOnAction(e -> handleResetDemoData());

        logoutMenuItem.setOnAction(e -> handleLogout());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(resetDemoItem, logoutMenuItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private HBox buildHeaderBar() {
        HBox headerBar = new HBox(AnthropicTheme.SPACE_16);
        headerBar.getStyleClass().add("app-header");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        Label brandLabel = AnthropicTheme.createHeadingLabel("Airline Reservation System", "heading-lg");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerLogoutButton.setOnAction(e -> handleLogout());
        headerLogoutButton.managedProperty().bind(headerLogoutButton.visibleProperty());

        HBox sessionPanel = new HBox(AnthropicTheme.SPACE_12, userStatusLabel, roleBadgeLabel, headerLogoutButton);
        sessionPanel.setAlignment(Pos.CENTER_RIGHT);

        headerBar.getChildren().addAll(brandLabel, spacer, sessionPanel);
        return headerBar;
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
        dialog.showAndWait();
        Passenger registered = dialog.getRegisteredPassenger();
        if (registered != null) {
            loginView.prefillIdentifier(registered.getId());
        }
    }

    private void handleLogout() {
        controller.logout();
        loginView.clearInputs();
        updateSessionHeader();
        switchContent(loginView);
    }

    private void handleResetDemoData() {
        controller.resetDemoData();
        loginView.clearInputs();
        updateSessionHeader();
        switchContent(loginView);
        AnthropicTheme.showInfo(
                stage,
                "Demo Reset",
                "Demo data has been restored to its initial state."
        );
    }

    private void switchContent(Node view) {
        contentContainer.getChildren().setAll(view);
    }

    private void updateSessionHeader() {
        Person currentUser = controller.getCurrentUser();
        roleBadgeLabel.getStyleClass().removeAll("badge-guest", "badge-passenger", "badge-admin");

        if (currentUser == null) {
            userStatusLabel.setText("Not signed in");
            roleBadgeLabel.setText("Guest");
            roleBadgeLabel.getStyleClass().add("badge-guest");
            headerLogoutButton.setVisible(false);
            logoutMenuItem.setDisable(true);
            return;
        }

        userStatusLabel.setText(currentUser.getFullName() + " (" + currentUser.getId() + ")");
        roleBadgeLabel.setText(currentUser.getRole());
        roleBadgeLabel.getStyleClass().add(controller.isAdminLoggedIn() ? "badge-admin" : "badge-passenger");
        headerLogoutButton.setVisible(true);
        logoutMenuItem.setDisable(false);
    }
}
