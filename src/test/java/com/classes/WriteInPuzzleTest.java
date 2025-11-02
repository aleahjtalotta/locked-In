package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WriteInPuzzleTest {

    private WriteInPuzzle createPuzzle(String correctAnswer) {
        return new WriteInPuzzle(
                UUID.randomUUID(),
                2L,
                "Riddle",
                "Answer the riddle correctly.",
                "A shiny key",
                correctAnswer,
                false
        );
    }

    @Test
    public void isCorrectAnswerReturnsTrueWhenAnswerMatchesIgnoringCase() {
        WriteInPuzzle puzzle = createPuzzle("Echo");

        assertTrue(puzzle.isCorrectAnswer("echo"));
    }

    @Test
    public void isCorrectAnswerReturnsFalseWhenAnswerDoesNotMatch() {
        WriteInPuzzle puzzle = createPuzzle("Echo");

        assertFalse(puzzle.isCorrectAnswer("shadow"));
    }

    @Test
    public void isCorrectAnswerTreatsNullAnswerAsEmptyString() {
        WriteInPuzzle puzzle = createPuzzle("");

        assertTrue(puzzle.isCorrectAnswer(null));
    }

    @Test
    public void constructorDefaultsNullCorrectAnswerToEmptyString() {
        WriteInPuzzle puzzle = createPuzzle(null);

        assertEquals("", puzzle.getCorrectAnswer());
    }
}
