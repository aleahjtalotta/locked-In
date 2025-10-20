package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player of the escape room.
 */
public class Player {
    private final UUID id;
    private final Integer legacyId;
    private String name;
    private String email;
    private String avatar;
    private final ItemList inventory;
    private final Statistics statistics;
    private int currentScore;

    public Player(UUID id, Integer legacyId, String name, String email, String avatar,
                  ItemList inventory, Statistics statistics, int currentScore) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.name = Objects.requireNonNullElse(name, "Unknown Player");
        this.email = Objects.requireNonNullElse(email, "");
        this.avatar = avatar;
        this.inventory = inventory == null ? new ItemList() : inventory;
        this.statistics = statistics == null ? new Statistics() : statistics;
        this.currentScore = Math.max(0, currentScore);
    }

    public UUID getId() {
        return id;
    }

    public Integer getLegacyId() {
        return legacyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, this.name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNullElse(email, this.email);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ItemList getInventory() {
        return inventory;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void addScore(int delta) {
        currentScore = Math.max(0, currentScore + delta);
    }
}
