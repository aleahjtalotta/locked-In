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

    public Item(UUID id, Long legacyId, String name, boolean reusable) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.name = Objects.requireNonNullElse(name, "Unknown Item");
        this.reusable = reusable;
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

    public boolean isReusable() {
        return reusable;
    }
}
