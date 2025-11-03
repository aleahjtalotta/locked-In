package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RiddlePuzzleTest {

    private RiddlePuzzle createPuzzle(String riddle, String answer, boolean solved) {
        return new RiddlePuzzle(
                UUID.randomUUID(),
                42L,
                "Bridge Riddle",
                "Solve the classic riddle.",
                "Cross the bridge",
                riddle,
                answer,
                solved
        );
    }

    @Test
    public void isCorrectAnswerReturnsTrueWhenResponseMatchesIgnoringCase() {
        RiddlePuzzle puzzle = createPuzzle("What walks on four legs in the morning?", "Human", false);
        assertTrue(puzzle.isCorrectAnswer("HUMAN"));
    }

    @Test
    public void isCorrectAnswerReturnsFalseWhenResponseDiffers() {
        RiddlePuzzle puzzle = createPuzzle("What has keys but cannot open locks?", "Piano", false);
        assertFalse(puzzle.isCorrectAnswer("keyboard"));
    }

    @Test
    public void isCorrectAnswerTreatsNullResponseAsEmptyString() {
        RiddlePuzzle puzzle = createPuzzle("Invisible prompt", "", false);
        assertTrue(puzzle.isCorrectAnswer(null));
    }

    @Test
    public void constructorDefaultsNullRiddleAndAnswerToEmptyStrings() {
        RiddlePuzzle puzzle = createPuzzle(null, null, false);
        assertEquals("", puzzle.getRiddle());
        assertEquals("", puzzle.getAnswer());
    }
}
