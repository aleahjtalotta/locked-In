package com.classes;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class ProgressTest {

    @Test
    public void resetClearsCurrentRoomAndSolvedPuzzles() {
        Progress progress = new Progress();
        UUID originalPlayer = UUID.randomUUID();
        progress.setActivePlayerId(originalPlayer);
        progress.setCurrentRoomId(UUID.randomUUID());
        UUID solvedPuzzle = UUID.randomUUID();
        progress.markPuzzleSolved(solvedPuzzle);

        UUID newPlayer = UUID.randomUUID();
        progress.reset(newPlayer);

        assertEquals(newPlayer, progress.getActivePlayerId());
        assertNull(progress.getCurrentRoomId());
        assertFalse(progress.isPuzzleSolved(solvedPuzzle));
        assertTrue(progress.getSolvedPuzzleIds().isEmpty());
    }

    @Test
    public void markPuzzleSolvedTracksSolvedPuzzles() {
        Progress progress = new Progress();
        UUID puzzleId = UUID.randomUUID();

        progress.markPuzzleSolved(null);
        progress.markPuzzleSolved(puzzleId);

        assertTrue(progress.isPuzzleSolved(puzzleId));
        assertFalse(progress.isPuzzleSolved(UUID.randomUUID()));
        assertEquals(1, progress.getSolvedPuzzleIds().size());
    }

    @Test
    public void loadSolvedPuzzlesReplacesExistingSet() {
        Progress progress = new Progress();
        UUID oldPuzzle = UUID.randomUUID();
        progress.markPuzzleSolved(oldPuzzle);

        UUID newPuzzle = UUID.randomUUID();
        Set<UUID> solved = new HashSet<>();
        solved.add(newPuzzle);
        progress.loadSolvedPuzzles(solved);

        assertFalse(progress.isPuzzleSolved(oldPuzzle));
        assertTrue(progress.isPuzzleSolved(newPuzzle));

        progress.loadSolvedPuzzles(null);
        assertTrue(progress.getSolvedPuzzleIds().isEmpty());
    }

    @Test
    public void loadSolvedPuzzlesIgnoresNullEntries() {
        Progress progress = new Progress();
        UUID validPuzzle = UUID.randomUUID();
        java.util.List<UUID> puzzles = new java.util.ArrayList<>();
        puzzles.add(validPuzzle);
        puzzles.add(null);

        progress.loadSolvedPuzzles(puzzles);

        assertTrue(progress.isPuzzleSolved(validPuzzle));
        assertFalse("Null entries should not be retained", progress.getSolvedPuzzleIds().contains(null));
    }
}
