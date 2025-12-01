package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.Puzzle;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.application.Platform;
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
        wireButton(root, "Quit Game").ifPresent(button -> button.setOnAction(this::handleQuitGame));
    }

    private void handleLeaderboard(ActionEvent event) {
        SceneNavigator.switchTo(event, "LeaderboardScreen.fxml");
    }

    private void handleCurrentScore(ActionEvent event) {
        SceneNavigator.switchTo(event, "UserScore.fxml");
    }

    private void handleLogout(ActionEvent event) {
        CountdownTimerManager.pauseAndPersist();
        SessionContext.clear();
        GameState.reset();
        SceneNavigator.resetHistory();
        SceneNavigator.switchToWithoutHistory(event, "WelcomeScreen.fxml");
    }

    private void handleQuitGame(ActionEvent event) {
        CountdownTimerManager.finalizeTimer();
        saveProgress();
        Platform.exit();
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

    private void saveProgress() {
        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return;
        }

        GameSystem system = systemOpt.get();
        if (GameState.room1Puzzle1Done) {
            markSolved(system, 301L);
        }
        if (GameState.room1Puzzle2Done) {
            markSolved(system, 302L);
        }
        if (GameState.room2Puzzle1Done) {
            markSolved(system, 303L);
        }
        if (GameState.room2Puzzle2Done) {
            markSolved(system, 304L);
        }
        if (GameState.room3Puzzle1Done) {
            markSolved(system, 305L);
        }
        if (GameState.room3Puzzle2Done) {
            markSolved(system, 306L);
        }

        DataWriter writer = new DataWriter(dataDir);
        writer.saveGame(system);
    }

    private void markSolved(GameSystem system, Long legacyPuzzleId) {
        if (legacyPuzzleId == null) {
            return;
        }
        Optional<Puzzle> puzzleOpt = system.getPuzzles().asList().stream()
                .filter(p -> legacyPuzzleId.equals(p.getLegacyId()))
                .findFirst();
        if (puzzleOpt.isEmpty()) {
            return;
        }
        Puzzle puzzle = puzzleOpt.get();
        puzzle.markSolved();
        system.getProgress().markPuzzleSolved(puzzle.getId());
        SessionContext.getActivePlayer().ifPresent(player -> player.markPuzzleSolved(puzzle.getId()));
    }
}
