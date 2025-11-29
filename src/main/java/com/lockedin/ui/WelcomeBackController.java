package com.lockedin.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WelcomeBackController {

    @FXML
    private void handleEnterMansion(ActionEvent event) {
        navigateByProgress(event);
    }

    private void navigateByProgress(ActionEvent event) {
        int solved = SessionContext.getSolvedPuzzleCount();
        String target = ProgressNavigator.destinationForSolvedCount(solved);
        SceneNavigator.switchTo(event, target);
    }
}
