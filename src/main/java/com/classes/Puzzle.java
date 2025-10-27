package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Base abstraction for all puzzle implementations, encapsulating common metadata
 * (identity, description, reward, type) and solved state handling.
 */
public abstract class Puzzle {
    private final UUID id;
    private final Long legacyId;
    private final String name;
    private final String description;
    private final String reward;
    private final PuzzleType type;
    private boolean solved;

    /**
     * Constructs a puzzle with its identifying metadata and solved state.
     *
     * @param id the immutable identifier for the puzzle; must not be {@code null}
     * @param legacyId an optional legacy identifier used by older systems
     * @param name the display name presented to players; defaults to {@code "Puzzle"} when {@code null}
     * @param description a human-readable description of the puzzle; defaults to empty when {@code null}
     * @param reward the reward text earned by solving; defaults to empty when {@code null}
     * @param type the puzzle type classification; must not be {@code null}
     * @param solved {@code true} when the puzzle starts in the solved state
     */
    protected Puzzle(UUID id, Long legacyId, String name, String description, String reward, PuzzleType type, boolean solved) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.name = Objects.requireNonNullElse(name, "Puzzle");
        this.description = Objects.requireNonNullElse(description, "");
        this.reward = Objects.requireNonNullElse(reward, "");
        this.type = Objects.requireNonNull(type, "type");
        this.solved = solved;
    }

    /**
     * @return the immutable identifier associated with this puzzle
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return the legacy identifier used by older systems, or {@code null} when none exists
     */
    public Long getLegacyId() {
        return legacyId;
    }

    /**
     * @return the display name presented to players
     */
    public String getName() {
        return name;
    }

    /**
     * @return the human-readable description shown alongside the puzzle
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the reward text awarded once the puzzle is solved
     */
    public String getReward() {
        return reward;
    }

    /**
     * @return the puzzle type classification
     */
    public PuzzleType getType() {
        return type;
    }

    /**
     * @return {@code true} when the puzzle has been marked solved
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * Marks the puzzle as solved. Subsequent calls to {@link #isSolved()} return {@code true}.
     */
    public void markSolved() {
        this.solved = true;
    }

    /**
     * Resets the solved state so that {@link #isSolved()} returns {@code false}.
     */
    public void reset() {
        this.solved = false;
    }

    /**
     * Evaluates whether the supplied answer satisfies the puzzle-specific criteria.
     *
     * @param answer the player-submitted answer to validate; handling of {@code null} values is implementation-specific
     * @return {@code true} when the answer solves the puzzle
     */
    public abstract boolean isCorrectAnswer(String answer);
}
