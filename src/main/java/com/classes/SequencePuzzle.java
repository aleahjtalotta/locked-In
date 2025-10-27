package com.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Puzzle variant that evaluates answers as ordered sequences, normalizing whitespace and case.
 */
public class SequencePuzzle extends Puzzle {
    private final List<String> expectedSequence;

    /**
     * Creates a sequence puzzle where the supplied answers must match an expected order.
     *
     * @param id the unique identifier for the puzzle
     * @param legacyId the legacy identifier if one exists
     * @param name the display name for the puzzle
     * @param description a human-readable description of the puzzle
     * @param reward the reward text earned upon solving
     * @param expectedSequence the sequence players must match to solve the puzzle
     * @param solved whether the puzzle starts in a solved state
     */
    public SequencePuzzle(UUID id, Long legacyId, String name, String description, String reward,
                          List<String> expectedSequence, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.SEQUENCE, solved);
        this.expectedSequence = expectedSequence == null ? new ArrayList<>() : new ArrayList<>(expectedSequence);
    }

    /**
     * @return an immutable view of the expected answer sequence
     */
    public List<String> getExpectedSequence() {
        return List.copyOf(expectedSequence);
    }

    /**
     * Validates that the provided answer represents the expected sequence when normalized.
     *
     * @param answer the player-submitted answer to validate
     * @return {@code true} when the answer matches the configured sequence ignoring whitespace and case
     */
    @Override
    public boolean isCorrectAnswer(String answer) {
        if (answer == null) {
            return false;
        }
        String normalized = answer.trim().replaceAll("\\s+", " ");
        String expected = String.join(" ", expectedSequence).trim().replaceAll("\\s+", " ");
        return Objects.equals(normalized.toLowerCase(), expected.toLowerCase());
    }
}
