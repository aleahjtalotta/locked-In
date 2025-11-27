package com.lockedin.ui;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelcomeBackController {

    @FXML
    private void handleEnterMansion(ActionEvent event) {
        navigateByProgress(event);
    }

    private void navigateByProgress(ActionEvent event) {
        int solved = SessionContext.getSolvedPuzzleCount();
        String target = ProgressNavigator.destinationForSolvedCount(solved);
        try {
            switchScene(event, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(ActionEvent event, String resourcePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(LockedInApp.class.getResource(resourcePath));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
