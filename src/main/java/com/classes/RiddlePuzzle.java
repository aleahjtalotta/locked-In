package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Puzzle variant that presents a riddle and validates the submitted solution, ignoring case.
 */
public class RiddlePuzzle extends Puzzle {
    private final String riddle;
    private final String answer;

    /**
     * Creates a riddle puzzle consisting of a prompt and answer.
     *
     * @param id the unique identifier for the puzzle
     * @param legacyId the legacy identifier if one exists
     * @param name the display name for the puzzle
     * @param description a human-readable description of the puzzle
     * @param reward the reward text earned upon solving
     * @param riddle the riddle prompt shown to players
     * @param answer the correct answer to the riddle
     * @param solved whether the puzzle starts in a solved state
     */
    public RiddlePuzzle(UUID id, Long legacyId, String name, String description, String reward,
                        String riddle, String answer, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.RIDDLE, solved);
        this.riddle = Objects.requireNonNullElse(riddle, "");
        this.answer = Objects.requireNonNullElse(answer, "");
    }

    /**
     * @return the riddle prompt presented to players
     */
    public String getRiddle() {
        return riddle;
    }

    /**
     * @return the configured solution to the riddle
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Checks whether the provided response matches the riddle answer, ignoring case.
     *
     * @param response the player-submitted answer to validate
     * @return {@code true} when the response equals the configured answer, ignoring case
     */
    @Override
    public boolean isCorrectAnswer(String response) {
        return answer.equalsIgnoreCase(Objects.requireNonNullElse(response, ""));
    }
}
