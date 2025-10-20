package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single hint that can be surfaced to the player.
 */
public class Hint {
    private final UUID id;
    private final String text;

    public Hint(UUID id, String text) {
        this.id = Objects.requireNonNull(id, "id");
        this.text = Objects.requireNonNullElse(text, "");
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
