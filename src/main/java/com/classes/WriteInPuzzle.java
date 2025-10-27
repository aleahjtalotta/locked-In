package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Puzzle variant that validates free-form submissions against a configured answer, ignoring case.
 */
public class WriteInPuzzle extends Puzzle {
    private final String correctAnswer;

    /**
     * Creates a write-in puzzle instance backed by a single correct answer.
     *
     * @param id the unique identifier for the puzzle
     * @param legacyId the legacy identifier if one exists
     * @param name the display name for the puzzle
     * @param description a human-readable description of the puzzle
     * @param reward the reward text earned upon solving
     * @param correctAnswer the expected answer players must submit
     * @param solved whether the puzzle starts in a solved state
     */
    public WriteInPuzzle(UUID id, Long legacyId, String name, String description, String reward,
                         String correctAnswer, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.WRITE_IN, solved);
        this.correctAnswer = Objects.requireNonNullElse(correctAnswer, "");
    }

    /**
     * @return the configured correct answer for the puzzle
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * Checks whether the provided answer matches the configured solution, ignoring case.
     *
     * @param answer the player-submitted answer to validate
     * @return {@code true} when the answer equals the configured solution, ignoring case
     */
    @Override
    public boolean isCorrectAnswer(String answer) {
        return correctAnswer.equalsIgnoreCase(Objects.requireNonNullElse(answer, ""));
    }
}
