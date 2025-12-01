package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.Player;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WelcomeController {

    @FXML
    private void handleLogin(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/Login.fxml");
    }

    @FXML
    private void handleLeaderboard(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/LeaderboardScreen.fxml");
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/SignUp.fxml");
    }

    @FXML
    private void handleQuit(ActionEvent event) {
        CountdownTimerManager.finalizeTimer();
        persistSessionProgress();
        Platform.exit();
    }

    private void persistSessionProgress() {
        Optional<Player> active = SessionContext.getActivePlayer();
        if (active.isEmpty()) {
            return;
        }
        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return;
        }
        GameSystem system = systemOpt.get();
        Player sessionPlayer = active.get();
        system.getPlayers().findById(sessionPlayer.getId()).ifPresent(saved -> {
            saved.setSolvedPuzzleIds(sessionPlayer.getSolvedPuzzleIds());
            int delta = sessionPlayer.getCurrentScore() - saved.getCurrentScore();
            if (delta != 0) {
                saved.addScore(delta);
            }
        });
        new DataWriter(dataDir).saveGame(system);
    }
}
