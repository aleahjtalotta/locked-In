package com.classes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class MultipleChoicePuzzleTest {

    @Test
    public void isCorrectAnswerReturnsTrueForCaseInsensitiveMatch() {
        MultipleChoicePuzzle puzzle = new MultipleChoicePuzzle(
                UUID.randomUUID(),
                1L,
                "Color Puzzle",
                "Pick the color of the sky",
                "Advance",
                Arrays.asList("blue", "green", "red"),
                "Blue",
                false
        );

        assertTrue(puzzle.isCorrectAnswer("bLuE"));
    }

    @Test
    public void isCorrectAnswerReturnsFalseForIncorrectAnswer() {
        MultipleChoicePuzzle puzzle = new MultipleChoicePuzzle(
                UUID.randomUUID(),
                2L,
                "Math Puzzle",
                "Select the even number",
                "Coins",
                Arrays.asList("1", "2", "3"),
                "2",
                false
        );

        assertFalse(puzzle.isCorrectAnswer("3"));
    }

    @Test
    public void isCorrectAnswerReturnsFalseWhenAnswerIsNull() {
        MultipleChoicePuzzle puzzle = new MultipleChoicePuzzle(
                UUID.randomUUID(),
                3L,
                "Word Puzzle",
                "Select the palindrome",
                "Points",
                Arrays.asList("cat", "level", "bird"),
                "level",
                false
        );

        assertFalse(puzzle.isCorrectAnswer(null));
    }

    @Test
    public void constructorCopiesOptionsToPreventExternalMutation() {
        List<String> originalOptions = new ArrayList<>(Arrays.asList("alpha", "beta"));
        MultipleChoicePuzzle puzzle = new MultipleChoicePuzzle(
                UUID.randomUUID(),
                4L,
                "Greek Letters",
                "Pick alpha",
                "Badge",
                originalOptions,
                "alpha",
                false
        );

        originalOptions.add("gamma");

        List<String> storedOptions = puzzle.getOptions();
        assertEquals(2, storedOptions.size());
        assertEquals(Arrays.asList("alpha", "beta"), storedOptions);
    }

    @Test
    public void getOptionsReturnsUnmodifiableCopy() {
        MultipleChoicePuzzle puzzle = new MultipleChoicePuzzle(
                UUID.randomUUID(),
                5L,
                "Directions",
                "Select north",
                "Map",
                Arrays.asList("north", "south", "east"),
                "north",
                false
        );

        List<String> options = puzzle.getOptions();

        try {
            options.add("west");
            fail("Expected UnsupportedOperationException when modifying options.");
        } catch (UnsupportedOperationException expected) {
            // expected path
        }
    }
}

