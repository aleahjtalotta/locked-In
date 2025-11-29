package com.lockedin.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WelcomeNewUserController {

    @FXML
    private void handleEnterMansion(ActionEvent event) {
        navigateByProgress(event);
    }

    @FXML
    private void handlePause(ActionEvent event) {
        SceneNavigator.switchTo(event, "PauseScreen.fxml");
    }

    private void navigateByProgress(ActionEvent event) {
        int solved = SessionContext.getSolvedPuzzleCount();
        String target = ProgressNavigator.destinationForSolvedCount(solved);
        SceneNavigator.switchTo(event, target);
    }
}
