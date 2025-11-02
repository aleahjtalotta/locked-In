package com.classes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

/**
 * Unit tests for {@link CodeLockPuzzle} exercising expected behaviour and
 * highlighting current defects.
 */
public class CodeLockPuzzleTest {

    private static final UUID PUZZLE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    public void isCorrectAnswer_matchesIgnoringCase() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(PUZZLE_ID, null, "Vault", "Pick the right code",
                "Treasure", "OPEN123", false);

        assertTrue(puzzle.isCorrectAnswer("open123"));
    }

    @Test
    public void isCorrectAnswer_returnsFalseForWrongCode() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(PUZZLE_ID, null, "Vault", "Pick the right code",
                "Treasure", "1234", false);

        assertFalse(puzzle.isCorrectAnswer("5678"));
    }

    @Test
    public void isCorrectAnswer_allowsTrailingWhitespace() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(PUZZLE_ID, null, "Vault", "Pick the right code",
                "Treasure", "4321", false);

        assertTrue("Answer with incidental whitespace should still solve the lock",
                puzzle.isCorrectAnswer("4321 "));
    }

    @Test
    public void isCorrectAnswer_withoutConfiguredCode_shouldNotAutoSolve() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(PUZZLE_ID, null, "Vault", "Pick the right code",
                "Treasure", null, false);

        assertFalse("Lock with no configured code should not auto-solve",
                puzzle.isCorrectAnswer(""));
    }

    @Test
    public void isCorrectAnswer_stripsNewlineFromSavedCode() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(PUZZLE_ID, null, "Vault", "Pick the right code",
                "Treasure", "9999\n", false);

        assertTrue("Trailing newline from saved code should be ignored",
                puzzle.isCorrectAnswer("9999"));
    }
}
