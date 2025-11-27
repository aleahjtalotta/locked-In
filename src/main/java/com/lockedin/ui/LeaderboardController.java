package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.GameSystem;
import com.classes.ScoreEntry;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller that fills the leaderboard screen with the top three scores.
 */
public class LeaderboardController {

    @FXML
    private Label leaderboardLabel;

    @FXML
    public void initialize() {
        populateTopThree();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader =
                new FXMLLoader(LockedInApp.class.getResource("/com/ourgroup1/WelcomeScreen.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void populateTopThree() {
        List<ScoreEntry> topThree = loadTopThreeScores();
        if (topThree.isEmpty()) {
            leaderboardLabel.setText("No scores available yet.");
            return;
        }

        String[] positions = {"1st", "2nd", "3rd"};
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            if (i > 0) {
                text.append("\n\n");
            }
            text.append(positions[i]).append(": ");
            if (i < topThree.size()) {
                ScoreEntry entry = topThree.get(i);
                text.append(entry.getPlayerName())
                        .append(" - ")
                        .append(entry.getScore())
                        .append(" points");
            } else {
                text.append("N/A");
            }
        }
        leaderboardLabel.setText(text.toString());
    }

    private List<ScoreEntry> loadTopThreeScores() {
        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return Collections.emptyList();
        }

        return systemOpt.get().getLeaderboard().getScores().stream()
                .limit(3)
                .toList();
    }
}
