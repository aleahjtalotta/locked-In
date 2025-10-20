package com.classes;

import java.util.Objects;
import java.util.UUID;

public abstract class Puzzle {
    private final UUID id;
    private final Long legacyId;
    private final String name;
    private final String description;
    private final String reward;
    private final PuzzleType type;
    private boolean solved;

    protected Puzzle(UUID id, Long legacyId, String name, String description, String reward, PuzzleType type, boolean solved) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.name = Objects.requireNonNullElse(name, "Puzzle");
        this.description = Objects.requireNonNullElse(description, "");
        this.reward = Objects.requireNonNullElse(reward, "");
        this.type = Objects.requireNonNull(type, "type");
        this.solved = solved;
    }

    public UUID getId() {
        return id;
    }

    public Long getLegacyId() {
        return legacyId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getReward() {
        return reward;
    }

    public PuzzleType getType() {
        return type;
    }

    public boolean isSolved() {
        return solved;
    }

    public void markSolved() {
        this.solved = true;
    }

    public void reset() {
        this.solved = false;
    }

    public abstract boolean isCorrectAnswer(String answer);
}
