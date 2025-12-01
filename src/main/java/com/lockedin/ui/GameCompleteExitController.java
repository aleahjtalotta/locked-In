package com.lockedin.ui;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Handles navigation for the game completion screen.
 */
public class GameCompleteExitController implements SceneBindable {

    @Override
    public void onSceneLoaded(Parent root) {
        wireButton(root, "Leave").ifPresent(button -> button.setOnAction(this::handleLeave));
        wireButton(root, "Pause").ifPresent(button -> button.setOnAction(this::handlePause));
    }

    @FXML
    private void handleLeave(ActionEvent event) {
        CountdownTimerManager.finalizeTimer();
        SceneNavigator.switchTo(event, "LogOutScreen.fxml");
    }

    @FXML
    private void handlePause(ActionEvent event) {
        CountdownTimerManager.pauseAndPersist();
        SceneNavigator.switchTo(event, "PauseScreen.fxml");
    }

    private Optional<Button> wireButton(Parent root, String text) {
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> text.equals(btn.getText()))
                .findFirst();
    }
}
