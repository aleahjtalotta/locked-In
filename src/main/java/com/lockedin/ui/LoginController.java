package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.GameSystem;
import com.classes.Player;
import com.classes.PlayerList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

        Optional<Player> user = findUser(name, email);
        if (user.isEmpty()) {
            errorLabel.setText("User not found.");
            return;
        }

        SessionContext.setActivePlayer(user.get());
        // Successful login: navigate to the Welcome Back screen.
        try {
            FXMLLoader loader =
                    new FXMLLoader(LockedInApp.class.getResource("/com/ourgroup1/WelcomeBackScreen.fxml"));
            Scene scene = new Scene(loader.load());
            LockedInApp.applyGlobalStyles(scene);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Unable to load Welcome Back screen.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        switchScene(event, "/com/ourgroup1/WelcomeScreen.fxml");
    }

    private Optional<Player> findUser(String name, String email) {
        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> system = loader.loadGame();
        if (system.isEmpty()) {
            return Optional.empty();
        }
        PlayerList players = system.get().getPlayers();
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

    private void switchScene(ActionEvent event, String resourcePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(LockedInApp.class.getResource(resourcePath));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
