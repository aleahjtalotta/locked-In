package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single hint that can be surfaced to the player.
 */
public class Hint {
    private final UUID id;
    private final Long legacyId;
    private final String text;

    public Hint(UUID id, Long legacyId, String text) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.text = Objects.requireNonNullElse(text, "");
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Long getLegacyId() {
        return legacyId;
    }
}
