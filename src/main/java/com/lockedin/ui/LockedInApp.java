package com.lockedin.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LockedInApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the primary.fxml you made in SceneBuilder.
        // This path matches: src/main/resources/com/ourgroup1/primary.fxml
        FXMLLoader fxmlLoader =
                new FXMLLoader(LockedInApp.class.getResource("/com/ourgroup1/primary.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Locked In");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();   // starts JavaFX -> calls start(Stage)
    }
}