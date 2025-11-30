package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.GameSystem;
import com.classes.Player;
import com.classes.PlayerList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the login/sign-up screen that collects name + email and
 * validates against users.json.
 */
public class LoginController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
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

        Optional<Player> user = findUser(systemOpt.get().getPlayers(), name, email);
        if (user.isEmpty()) {
            errorLabel.setText("User not found.");
            return;
        }

        SessionContext.setActivePlayer(user.get());
        GameState.syncFrom(systemOpt.get(), user.get());
        // Successful login: navigate to the Welcome Back screen.
        SceneNavigator.switchTo(event, "/com/ourgroup1/WelcomeBackScreen.fxml");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
    }

    private Optional<Player> findUser(PlayerList players, String name, String email) {
        String targetName = normalize(name);
        String targetEmail = normalize(email);
        return players.asList().stream()
                .filter(p -> normalize(p.getName()).equals(targetName))
                .filter(p -> normalize(p.getEmail()).equals(targetEmail))
                .findFirst();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
