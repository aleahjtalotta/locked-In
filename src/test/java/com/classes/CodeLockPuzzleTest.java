package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class CodeLockPuzzleTest {

    @Test
    public void isCorrectAnswerAcceptsMatchingCodeIgnoringCase() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(
                UUID.fromString("00000000-0000-0000-0000-000000000111"),
                7L, "Vault", "Enter the vault code", "Gold Key", "OpenSesame", false);

        assertTrue(puzzle.isCorrectAnswer("opensesame"));
        assertTrue(puzzle.isCorrectAnswer("OPENSESAME"));
    }

    @Test
    public void isCorrectAnswerRejectsWrongCodes() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(
                UUID.fromString("00000000-0000-0000-0000-000000000222"),
                null, "Safe", "Unlock the safe", "Map Fragment", "1234", false);

        assertFalse(puzzle.isCorrectAnswer("4321"));
        assertFalse(puzzle.isCorrectAnswer("12345"));
    }

    @Test
    public void isCorrectAnswerReturnsFalseForNullInputWhenCodeNotEmpty() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(
                UUID.fromString("00000000-0000-0000-0000-000000000333"),
                null, "Locker", "Enter the locker code", "Documents", "ABC", false);

        assertFalse(puzzle.isCorrectAnswer(null));
    }

    @Test
    public void constructorReplacesNullCodeWithEmptyString() {
        CodeLockPuzzle puzzle = new CodeLockPuzzle(
                UUID.fromString("00000000-0000-0000-0000-000000000444"),
                null, "Door", "Default code", "Supplies", null, false);

        assertEquals("", puzzle.getCode());
        assertTrue(puzzle.isCorrectAnswer(""));
        assertTrue(puzzle.isCorrectAnswer(null));
    }
}
