package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Puzzle variant that validates answers against a single fixed code, ignoring case.
 */
public class CodeLockPuzzle extends Puzzle {
    private final String code;

    /**
     * Creates a code lock puzzle with the expected code answer.
     *
     * @param id the unique identifier for the puzzle
     * @param legacyId the legacy identifier if one exists
     * @param name the display name for the puzzle
     * @param description a human-readable description of the puzzle
     * @param reward the reward text earned upon solving
     * @param code the correct code players must submit
     * @param solved whether the puzzle starts in a solved state
     */
    public CodeLockPuzzle(UUID id, Long legacyId, String name, String description, String reward,
                          String code, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.CODE_LOCK, solved);
        this.code = Objects.requireNonNullElse(code, "");
    }

    /**
     * @return the code that constitutes the correct answer
     */
    public String getCode() {
        return code;
    }

    /**
     * Checks whether the supplied answer matches the configured code ignoring case.
     *
     * @param answer the player-submitted answer to validate
     * @return {@code true} when the answer equals the code, ignoring case
     */
    @Override
    public boolean isCorrectAnswer(String answer) {
        return code.equalsIgnoreCase(Objects.requireNonNullElse(answer, ""));
    }
}
