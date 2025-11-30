package com.lockedin.ui;

import java.time.Duration;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Handles navigation from the log out screen actions.
 */
public class LogOutController implements SceneBindable {

    @FXML
    private Label playerValueLabel;

    @FXML
    private Label timeValueLabel;

    @FXML
    private Label pointsValueLabel;

    @FXML
    private Label puzzlesValueLabel;

    @Override
    public void onSceneLoaded(Parent root) {
        wireButton(root, "Leaderboard").ifPresent(btn -> btn.setOnAction(this::handleLeaderboard));
        wireButton(root, "Log out").ifPresent(btn -> btn.setOnAction(this::handleLogout));
        wireButton(root, "Quit Game").ifPresent(btn -> btn.setOnAction(this::handleQuit));
        populateStats();
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

    private void populateStats() {
        Optional<com.classes.Player> activeOpt = SessionContext.getActivePlayer();
        if (activeOpt.isEmpty()) {
            setText(playerValueLabel, "Unknown");
            setText(timeValueLabel, "--:--:--");
            setText(pointsValueLabel, "0");
            setText(puzzlesValueLabel, "0");
            return;
        }
        com.classes.Player player = activeOpt.get();
        setText(playerValueLabel, player.getName());
        setText(pointsValueLabel, String.valueOf(player.getCurrentScore()));
        setText(puzzlesValueLabel, String.valueOf(player.getSolvedPuzzleIds().size()));
        Duration avg = player.getStatistics().getAverageCompletionTime();
        setText(timeValueLabel, formatDuration(avg));
    }

    private void setText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "--:--:--";
        }
        long seconds = Math.max(0, duration.getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
