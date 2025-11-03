package com.classes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class SequencePuzzleTest {

    @Test
    public void isCorrectAnswerMatchesIgnoringWhitespaceAndCase() {
        SequencePuzzle puzzle = new SequencePuzzle(UUID.randomUUID(), 1L, "Order the Steps",
                "Arrange actions", "Door opens", Arrays.asList("North", "East", "South"), false);

        assertTrue(puzzle.isCorrectAnswer(" north   EAST   south "));
        assertFalse(puzzle.isCorrectAnswer("north south east"));
    }

    @Test
    public void isCorrectAnswerRejectsNullAnswer() {
        SequencePuzzle puzzle = new SequencePuzzle(UUID.randomUUID(), null, "Mystery Order",
                "Solve the pattern", "Treasure", List.of("Alpha", "Beta"), false);

        assertFalse("Null answers should not be accepted as correct", puzzle.isCorrectAnswer(null));
    }

    @Test
    public void constructorCopiesExpectedSequence() {
        List<String> sequence = new ArrayList<>(List.of("First", "Second"));
        SequencePuzzle puzzle = new SequencePuzzle(UUID.randomUUID(), null, "Sequence",
                "Description", "Reward", sequence, false);

        sequence.add("Third");

        assertEquals(2, puzzle.getExpectedSequence().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getExpectedSequenceIsUnmodifiable() {
        SequencePuzzle puzzle = new SequencePuzzle(UUID.randomUUID(), null, "Immutable",
                "Description", "Reward", List.of("One", "Two"), false);

        puzzle.getExpectedSequence().add("Three");
    }
}
