package com.lockedin.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.io.IOException;

public class LockedInApp extends Application {

    private static final String GLOBAL_STYLESHEET = "/com/ourgroup1/styles.css";

    @Override
    public void start(Stage stage) throws IOException {
        // Load the primary.fxml you made in SceneBuilder.
        // This path matches: src/main/resources/com/ourgroup1/primary.fxml
        FXMLLoader fxmlLoader =
                new FXMLLoader(LockedInApp.class.getResource("/com/ourgroup1/WelcomeScreen.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        applyGlobalStyles(scene);
        scene.setUserData("/com/ourgroup1/WelcomeScreen.fxml");
        SceneNavigator.resetHistory();
        stage.setTitle("Locked In");
        stage.setScene(scene);
        stage.show();
    }

    public static void applyGlobalStyles(Scene scene) {
        URL stylesheet = LockedInApp.class.getResource(GLOBAL_STYLESHEET);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    public static void main(String[] args) {
        launch();   // starts JavaFX -> calls start(Stage)
    }
}
