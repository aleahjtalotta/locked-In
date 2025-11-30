package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.Player;
import com.classes.Puzzle;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class ProgressSaver {
    private static final Path DATA_DIR = Paths.get("JSON");

    private ProgressSaver() {
    }

    public static void recordSolved(Long puzzleLegacyId) {
        if (puzzleLegacyId == null) {
            return;
        }
        DataLoader loader = new DataLoader(DATA_DIR);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return;
        }

        GameSystem system = systemOpt.get();
        Optional<Puzzle> puzzleOpt = system.getPuzzles().asList().stream()
                .filter(p -> puzzleLegacyId.equals(p.getLegacyId()))
                .findFirst();
        if (puzzleOpt.isEmpty()) {
            return;
        }

        Puzzle puzzle = puzzleOpt.get();
        puzzle.markSolved();
        system.getProgress().markPuzzleSolved(puzzle.getId());

        Optional<Player> activePlayer = SessionContext.getActivePlayer();
        activePlayer.ifPresent(player -> {
            player.markPuzzleSolved(puzzle.getId());
            system.getPlayers().findById(player.getId()).ifPresent(p -> p.markPuzzleSolved(puzzle.getId()));
        });

        DataWriter writer = new DataWriter(DATA_DIR);
        if (writer.saveGame(system)) {
            activePlayer.ifPresent(player -> GameState.syncFrom(system, player));
        }
    }
}
