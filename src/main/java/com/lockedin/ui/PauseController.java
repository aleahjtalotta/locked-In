package com.lockedin.ui;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Handles routing from the pause screen buttons.
 */
public class PauseController implements SceneBindable {

    @Override
    public void onSceneLoaded(Parent root) {
        wireButton(root, "Leaderboard").ifPresent(button -> button.setOnAction(this::handleLeaderboard));
        wireButton(root, "Current Score").ifPresent(button -> button.setOnAction(this::handleCurrentScore));
        wireButton(root, "Log out").ifPresent(button -> button.setOnAction(this::handleLogout));
        wireButton(root, "Back").ifPresent(button -> button.setOnAction(this::handleBack));
    }

    private void handleLeaderboard(ActionEvent event) {
        SceneNavigator.switchTo(event, "LeaderboardScreen.fxml");
    }

    private void handleCurrentScore(ActionEvent event) {
        SceneNavigator.switchTo(event, "UserScore.fxml");
    }

    private void handleLogout(ActionEvent event) {
        SessionContext.clear();
        SceneNavigator.resetHistory();
        SceneNavigator.switchToWithoutHistory(event, "WelcomeScreen.fxml");
    }

    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
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
