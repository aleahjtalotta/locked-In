package com.lockedin.ui;

import com.classes.Puzzle;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Displays the active user's score, rooms completed, and puzzles solved.
 */
public class UserScoreController implements SceneBindable {

    private Label scoreDetailsLabel;

    @Override
    public void onSceneLoaded(Parent root) {
        this.scoreDetailsLabel = findScoreLabel(root);
        wireBackButton(root);
        populateStats();
    }

    private void populateStats() {
        Optional<com.classes.Player> activeOpt = SessionContext.getActivePlayer();
        if (activeOpt.isEmpty()) {
            setText("No active user.\n\nLog in to view your progress.");
            return;
        }
        com.classes.Player player = activeOpt.get();

        int score = player.getCurrentScore();
        int puzzlesSolved = player.getSolvedPuzzleIds().size();
        int roomsCompleted = countRoomsCompleted(player);

        String text = score + " Points\n\n"
                + roomsCompleted + " Rooms Completed\n\n"
                + puzzlesSolved + " Puzzles Solved";
        setText(text);
    }

    private int countRoomsCompleted(com.classes.Player player) {
        boolean room1 = isPuzzleSolved(player, 301L) && isPuzzleSolved(player, 302L);
        boolean room2 = isPuzzleSolved(player, 303L) && isPuzzleSolved(player, 304L);
        boolean room3 = isPuzzleSolved(player, 305L) && isPuzzleSolved(player, 306L);
        int count = 0;
        if (room1) {
            count++;
        }
        if (room2) {
            count++;
        }
        if (room3) {
            count++;
        }
        return count;
    }

    private boolean isPuzzleSolved(com.classes.Player player, Long legacyId) {
        return PuzzleProvider.findPuzzleByLegacyId(legacyId)
                .map(Puzzle::getId)
                .map(id -> player.getSolvedPuzzleIds().contains(id))
                .orElse(false);
    }

    private void setText(String value) {
        if (scoreDetailsLabel != null) {
            scoreDetailsLabel.setText(value);
        }
    }

    private Label findScoreLabel(Parent root) {
        var node = root.lookup("#scoreDetailsLabel");
        if (node instanceof Label label) {
            return label;
        }
        return null;
    }

    private void wireBackButton(Parent root) {
        findBackButton(root).ifPresent(button -> button.setOnAction(this::handleBack));
    }

    private Optional<Button> findBackButton(Parent root) {
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> "Back".equals(btn.getText()))
                .findFirst();
    }

    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
    }
}
