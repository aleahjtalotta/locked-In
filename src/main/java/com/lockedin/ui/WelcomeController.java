package com.lockedin.ui;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelcomeController {

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        // Swap to the login screen when the Log In button is pressed.
        switchToLogin(event);
    }

    @FXML
    private void handleLeaderboard(ActionEvent event) throws IOException {
        switchScene(event, "/com/ourgroup1/LeaderboardScreen.fxml");
    }

    @FXML
    private void handleSignup(ActionEvent event) throws IOException {
        // For now, sign up and login share the same screen (name + email).
        switchToLogin(event);
    }

    private void switchToLogin(ActionEvent event) throws IOException {
        switchScene(event, "/com/ourgroup1/Login.fxml");
    }

    private void switchScene(ActionEvent event, String resourcePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(LockedInApp.class.getResource(resourcePath));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
