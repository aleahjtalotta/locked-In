package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.Player;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;


public final class CompletionRecorder {
    private static final Path DATA_DIR = Paths.get("JSON");

    private CompletionRecorder() {
    }

    /**
     * Stops the timer and persists the remaining time to the active player's
     * average completion time once all rooms are complete.
     */
    public static void recordTimeIfComplete() {
        if (!(GameState.room1Complete && GameState.room2Complete && GameState.room3Complete)) {
            return;
        }
        CountdownTimerManager.finalizeTimer();
        Duration remaining = CountdownTimerManager.getRemainingDuration();
        Optional<Player> activeOpt = SessionContext.getActivePlayer();
        if (activeOpt.isEmpty()) {
            return;
        }
        Player sessionPlayer = activeOpt.get();
        sessionPlayer.getStatistics().setAverageCompletionTime(remaining);

        DataLoader loader = new DataLoader(DATA_DIR);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return;
        }
        GameSystem system = systemOpt.get();
        system.getPlayers()
                .findById(sessionPlayer.getId())
                .ifPresent(saved -> saved.getStatistics().setAverageCompletionTime(remaining));
        new DataWriter(DATA_DIR).saveGame(system);
    }
}
