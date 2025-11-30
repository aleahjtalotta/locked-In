package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.GameSystem;
import com.classes.Player;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

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
    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
    }

    private void populateTopThree() {
        List<Player> topThree = loadTopThreePlayers();
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
                Player player = topThree.get(i);
                text.append(player.getName())
                        .append(" - ")
                        .append(player.getCurrentScore())
                        .append(" points");
            } else {
                text.append("N/A");
            }
        }
        leaderboardLabel.setText(text.toString());
    }

    private List<Player> loadTopThreePlayers() {
        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return Collections.emptyList();
        }

        return systemOpt.get().getPlayers().asList().stream()
                .sorted(Comparator.comparingInt(Player::getCurrentScore).reversed()
                        .thenComparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(3)
                .toList();
    }
}
