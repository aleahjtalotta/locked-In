package com.lockedin.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WelcomeBackController {

    @FXML
    private Label puzzlesLabel;

    @FXML
    private Label timeLeftLabel;

    @FXML
    private Label inventoryItemsLabel;

    @FXML
    private void initialize() {
        populateStats();
    }

    @FXML
    private void handleEnterMansion(ActionEvent event) {
        navigateByProgress(event);
    }

    @FXML
    private void handlePause(ActionEvent event) {
        SceneNavigator.switchTo(event, "PauseScreen.fxml");
    }

    private void populateStats() {
        var activeOpt = SessionContext.getActivePlayer();
        if (activeOpt.isEmpty()) {
            setText(puzzlesLabel, "Puzzles: 0");
            setText(timeLeftLabel, "Time Left: --:--:--");
            setText(inventoryItemsLabel, "");
            return;
        }
        var player = activeOpt.get();
        int puzzlesSolved = player.getSolvedPuzzleIds().size();
        setText(puzzlesLabel, "Puzzles: " + puzzlesSolved);

        var avg = player.getStatistics().getAverageCompletionTime();
        setText(timeLeftLabel, "Time Left: " + formatDuration(avg));
        setText(inventoryItemsLabel, buildInventoryText());
    }

    private void setText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private String formatDuration(java.time.Duration duration) {
        if (duration == null) {
            return "--:--:--";
        }
        long seconds = Math.max(0, duration.getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private String buildInventoryText() {
        var items = InventoryManager.getItems();
        if (items.isEmpty()) {
            return "None";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i).name());
            if (i < items.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private void navigateByProgress(ActionEvent event) {
        int solved = SessionContext.getSolvedPuzzleCount();
        String target = ProgressNavigator.destinationForSolvedCount(solved);
        SceneNavigator.switchTo(event, target);
    }
}
