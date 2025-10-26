package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an item players can discover and use.
 */
public class Item {
    private final UUID id;
    private final Long legacyId;
    private final String name;
    private final boolean reusable;

    /**
     * Creates an item definition with identifiers, display name, and reuse behavior.
     *
     * @param id        globally unique identifier for the item
     * @param legacyId  optional legacy numeric identifier
     * @param name      display name shown to the player
     * @param reusable  whether the item can be used multiple times
     */
    public Item(UUID id, Long legacyId, String name, boolean reusable) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.name = Objects.requireNonNullElse(name, "Unknown Item");
        this.reusable = reusable;
    }

    /**
     * Returns the immutable unique identifier for the item.
     *
     * @return unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Provides the legacy numeric identifier if one exists.
     *
     * @return legacy id or {@code null}
     */
    public Long getLegacyId() {
        return legacyId;
    }

    /**
     * Retrieves the user-facing name of the item.
     *
     * @return item name
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether the item can be used repeatedly without consumption.
     *
     * @return {@code true} when the item is reusable
     */
    public boolean isReusable() {
        return reusable;
    }
}
