package com.lockedin.ui;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Centralizes scene switching and controller assignment for the puzzle flow.
 */
public final class SceneNavigator {
    private static final String RESOURCE_BASE = "/com/ourgroup1/";
    private static final Deque<String> HISTORY = new ArrayDeque<>();

    private static final Map<String, Supplier<? extends SceneBindable>> CONTROLLERS = Map.ofEntries(
            Map.entry("ChooseDoorScreen.fxml", ChooseDoorController::new),
            Map.entry("ChooseDoorRoom1Complete.fxml", ChooseDoorController::new),
            Map.entry("ChooseDoorRoom2Complete.fxml", ChooseDoorController::new),
            Map.entry("ChooseDoorRoom3Complete.fxml", ChooseDoorController::new),
            Map.entry("ChooseDoorRoom1and2Complete.fxml", ChooseDoorController::new),
            Map.entry("ChooseDoorRoom1and3Complete.fxml", ChooseDoorController::new),
            Map.entry("ChooseDoorRoom2and3Complete.fxml", ChooseDoorController::new),
            Map.entry("GameCompleteExit.fxml", GameCompleteExitController::new),
            Map.entry("LogOutScreen.fxml", LogOutController::new),
            Map.entry("PauseScreen.fxml", PauseController::new),
            Map.entry("UserScore.fxml", UserScoreController::new),
            Map.entry("Room1Puzzle1.fxml", Room1Puzzle1Controller::new),
            Map.entry("Room1Puzzle2.fxml", Room1Puzzle2Controller::new),
            Map.entry("Room2Puzzle1.fxml", Room2Puzzle1Controller::new),
            Map.entry("Room2Puzzle2.fxml", Room2Puzzle2Controller::new),
            Map.entry("Room3Puzzle1.fxml", Room3Puzzle1Controller::new),
            Map.entry("Room3Puzzle2.fxml", Room3Puzzle2Controller::new));

    private SceneNavigator() {
    }

    public static void switchTo(ActionEvent event, String fxmlName) {
        switchTo(event, fxmlName, true);
    }

    public static void back(ActionEvent event) {
        String previous = HISTORY.pollLast();
        if (previous != null) {
            switchTo(event, previous, false);
        }
    }

    public static void resetHistory() {
        HISTORY.clear();
    }

    public static void switchToWithoutHistory(ActionEvent event, String fxmlName) {
        switchTo(event, fxmlName, false);
    }

    private static void switchTo(ActionEvent event, String fxmlName, boolean pushCurrent) {
        String resourcePath = normalize(fxmlName);
        FXMLLoader loader = new FXMLLoader(LockedInApp.class.getResource(resourcePath));

        Optional<SceneBindable> maybeController = controllerFor(fxmlName);
        maybeController.ifPresent(loader::setController);

        try {
            Parent root = loader.load();
            maybeController.ifPresent(controller -> controller.onSceneLoaded(root));
            Scene scene = new Scene(root);
            LockedInApp.applyGlobalStyles(scene);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (pushCurrent) {
                Object current = stage.getScene() != null ? stage.getScene().getUserData() : null;
                if (current instanceof String) {
                    HISTORY.addLast((String) current);
                }
            }
            scene.setUserData(resourcePath);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load scene: " + resourcePath, e);
        }
    }

    private static String normalize(String fxmlName) {
        if (fxmlName.startsWith("/")) {
            return fxmlName;
        }
        return RESOURCE_BASE + fxmlName;
    }

    private static Optional<SceneBindable> controllerFor(String fxmlName) {
        String key = fxmlName.contains("/") ? fxmlName.substring(fxmlName.lastIndexOf('/') + 1) : fxmlName;
        Supplier<? extends SceneBindable> supplier = CONTROLLERS.get(key);
        if (supplier == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(supplier.get());
    }
}
