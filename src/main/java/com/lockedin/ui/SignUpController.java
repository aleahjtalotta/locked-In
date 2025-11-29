package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.PlayerList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the sign-up screen that registers a new user in users.json.
 */
public class SignUpController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        errorLabel.setText("");

        if (isBlank(name) || isBlank(email)) {
            errorLabel.setText("Please enter both name and email.");
            return;
        }

        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            errorLabel.setText("Unable to load user data.");
            return;
        }

        GameSystem system = systemOpt.get();
        PlayerList players = system.getPlayers();
        if (players.nameExists(name) || players.emailExists(email)) {
            errorLabel.setText("That user already exists, try again");
            return;
        }

        com.classes.Player newPlayer;
        try {
            newPlayer = players.createPlayer(name, email, null);
        } catch (IllegalArgumentException e) {
            errorLabel.setText("That user already exists, try again");
            return;
        }

        DataWriter writer = new DataWriter(dataDir);
        if (!writer.saveGame(system)) {
            errorLabel.setText("Unable to save new user.");
            return;
        }

        SessionContext.setActivePlayer(newPlayer);
        switchToWelcomeNewUser(event);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void switchToWelcomeNewUser(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/WelcomeNewUser.fxml");
    }
}
