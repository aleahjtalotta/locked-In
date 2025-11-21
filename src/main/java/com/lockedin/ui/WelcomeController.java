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
        FXMLLoader loader =
                new FXMLLoader(LockedInApp.class.getResource("/com/ourgroup1/Login.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
