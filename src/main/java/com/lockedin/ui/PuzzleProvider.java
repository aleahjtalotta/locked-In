package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.GameSystem;
import com.classes.Puzzle;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PuzzleProvider {
    private static final Path DATA_DIR = Paths.get("JSON");
    private static final Object LOCK = new Object();

    private static Map<Long, Puzzle> puzzlesByLegacyId;

    private PuzzleProvider() {
    }

    /**
     * Finds a puzzle by its legacy id (the {@code puzzleName} field in rooms.json).
     *
     * @param legacyId legacy numeric id to look up
     * @return optional puzzle when loaded successfully
     */
    public static Optional<Puzzle> findPuzzleByLegacyId(Long legacyId) {
        if (legacyId == null) {
            return Optional.empty();
        }
        synchronized (LOCK) {
            ensureLoaded();
            Puzzle puzzle = puzzlesByLegacyId.get(legacyId);
            return Optional.ofNullable(puzzle);
        }
    }

    private static void ensureLoaded() {
        if (puzzlesByLegacyId != null) {
            return;
        }
        DataLoader loader = new DataLoader(DATA_DIR);
        Optional<GameSystem> system = loader.loadGame();
        if (system.isEmpty()) {
            puzzlesByLegacyId = Collections.emptyMap();
            return;
        }
        Map<Long, Puzzle> map = new HashMap<>();
        system.get().getPuzzles().asList().forEach(puzzle -> {
            Long id = puzzle.getLegacyId();
            if (id != null && !map.containsKey(id)) {
                map.put(id, puzzle);
            }
        });
        puzzlesByLegacyId = map;
    }
}
