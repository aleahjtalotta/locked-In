package com.lockedin.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WelcomeController {

    @FXML
    private void handleLogin(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/Login.fxml");
    }

    @FXML
    private void handleLeaderboard(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/LeaderboardScreen.fxml");
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/SignUp.fxml");
    }
}
