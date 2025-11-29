package com.lockedin.ui;

import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Handles navigation from the log out screen actions.
 */
public class LogOutController implements SceneBindable {

    @Override
    public void onSceneLoaded(Parent root) {
        wireButton(root, "Leaderboard").ifPresent(btn -> btn.setOnAction(this::handleLeaderboard));
        wireButton(root, "Log out").ifPresent(btn -> btn.setOnAction(this::handleLogout));
        wireButton(root, "Quit Game").ifPresent(btn -> btn.setOnAction(this::handleQuit));
    }

    private void handleLeaderboard(ActionEvent event) {
        SceneNavigator.switchTo(event, "LeaderboardScreen.fxml");
    }

    private void handleLogout(ActionEvent event) {
        SessionContext.clear();
        SceneNavigator.resetHistory();
        SceneNavigator.switchToWithoutHistory(event, "WelcomeScreen.fxml");
    }

    private void handleQuit(ActionEvent event) {
        Platform.exit();
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
