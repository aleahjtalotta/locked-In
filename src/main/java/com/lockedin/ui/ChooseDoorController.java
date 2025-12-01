package com.lockedin.ui;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Shared controller for all hub screens that lead into room puzzles.
 */
public class ChooseDoorController implements SceneBindable {

    @Override
    public void onSceneLoaded(Parent root) {
        wireRoomButton(root, "1", this::handleRoom1, GameState.room1Complete);
        wireRoomButton(root, "2", this::handleRoom2, GameState.room2Complete);
        wireRoomButton(root, "3", this::handleRoom3, GameState.room3Complete);
        wirePauseButton(root);
    }

    @FXML
    private void goRoom1(ActionEvent event) {
        handleRoom1(event);
    }

    @FXML
    private void goRoom2(ActionEvent event) {
        handleRoom2(event);
    }

    @FXML
    private void goRoom3(ActionEvent event) {
        handleRoom3(event);
    }

    @FXML
    private void handlePause(ActionEvent event) {
        CountdownTimerManager.pauseAndPersist();
        SceneNavigator.switchTo(event, "PauseScreen.fxml");
    }

    private void handleRoom1(ActionEvent event) {
        if (GameState.room1Complete) {
            return;
        }
        if (!GameState.room1Puzzle1Done) {
            SceneNavigator.switchTo(event, "Room1Puzzle1.fxml");
        } else if (!GameState.room1Puzzle2Done) {
            SceneNavigator.switchTo(event, "Room1Puzzle2.fxml");
        }
    }

    private void handleRoom2(ActionEvent event) {
        if (GameState.room2Complete) {
            return;
        }
        if (!GameState.room2Puzzle1Done) {
            SceneNavigator.switchTo(event, "Room2Puzzle1.fxml");
        } else if (!GameState.room2Puzzle2Done) {
            SceneNavigator.switchTo(event, "Room2Puzzle2.fxml");
        }
    }

    private void handleRoom3(ActionEvent event) {
        if (GameState.room3Complete) {
            return;
        }
        if (!GameState.room3Puzzle1Done) {
            SceneNavigator.switchTo(event, "Room3Puzzle1.fxml");
        } else if (!GameState.room3Puzzle2Done) {
            SceneNavigator.switchTo(event, "Room3Puzzle2.fxml");
        }
    }

    private void wireRoomButton(Parent root, String buttonLabel, javafx.event.EventHandler<ActionEvent> handler,
            boolean completed) {
        findButtonWithText(root, buttonLabel).ifPresent(button -> {
            button.setOnAction(handler);
            button.setDisable(completed);
        });
    }

    private Optional<Button> findButtonWithText(Parent root, String text) {
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> text.equals(btn.getText()))
                .findFirst();
    }

    private void wirePauseButton(Parent root) {
        findButtonWithText(root, "Pause").ifPresent(button -> button.setOnAction(this::handlePause));
    }
}
