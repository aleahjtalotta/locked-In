package com.classes;

import org.junit.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.*;

public class PuzzleTest {

    @Test
    public void markSolvedSetsSolvedStateTrue() {
        TestPuzzle puzzle = new TestPuzzle(false, "answer");

        puzzle.markSolved();

        assertTrue(puzzle.isSolved());
    }

    @Test
    public void resetClearsSolvedState() {
        TestPuzzle puzzle = new TestPuzzle(true, "answer");

        puzzle.reset();

        assertFalse(puzzle.isSolved());
    }

    @Test
    public void constructorDefaultValuesWhenOptionalFieldsNull() {
        TestPuzzle puzzle = new TestPuzzle(false, "expected", null, null, null);

        assertEquals("Puzzle", puzzle.getName());
        assertEquals("", puzzle.getDescription());
        assertEquals("", puzzle.getReward());
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsWhenIdIsNull() {
        new TestPuzzle(null, PuzzleType.WRITE_IN, false, "Name", "Description", "Reward", "answer");
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsWhenTypeIsNull() {
        new TestPuzzle(UUID.randomUUID(), null, false, "Name", "Description", "Reward", "answer");
    }

    @Test
    public void isCorrectAnswerReturnsTrueWhenSubclassMatchesAnswer() {
        TestPuzzle puzzle = new TestPuzzle(false, "secret");

        assertTrue(puzzle.isCorrectAnswer("secret"));
    }

    @Test
    public void isCorrectAnswerReturnsFalseWhenSubclassRejectsAnswer() {
        TestPuzzle puzzle = new TestPuzzle(false, "secret");

        assertFalse(puzzle.isCorrectAnswer("wrong"));
    }

    private static final class TestPuzzle extends Puzzle {
        private final String correctAnswer;

        TestPuzzle(boolean solved, String correctAnswer) {
            this(UUID.randomUUID(), PuzzleType.WRITE_IN, solved, "Sample", "Description", "Reward", correctAnswer);
        }

        TestPuzzle(boolean solved, String correctAnswer, String name, String description, String reward) {
            this(UUID.randomUUID(), PuzzleType.WRITE_IN, solved,
                    name, description, reward, correctAnswer);
        }

        TestPuzzle(UUID id, PuzzleType type, boolean solved,
                   String name, String description, String reward, String correctAnswer) {
            super(id, 101L, name, description, reward, type, solved);
            this.correctAnswer = correctAnswer;
        }

        @Override
        public boolean isCorrectAnswer(String answer) {
            return Objects.equals(correctAnswer, answer);
        }
    }
}

