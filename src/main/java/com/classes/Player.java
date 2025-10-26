package com.classes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a player profile and their progress.
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
    private final Set<UUID> solvedPuzzleIds;

    public Player(UUID id, Integer legacyId, String name, String email, String avatar,
                  ItemList inventory, Statistics statistics, int currentScore,
                  Collection<UUID> solvedPuzzleIds) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.name = Objects.requireNonNullElse(name, "Unknown Player");
        this.email = Objects.requireNonNullElse(email, "");
        this.avatar = avatar;
        this.inventory = inventory == null ? new ItemList() : inventory;
        this.statistics = statistics == null ? new Statistics() : statistics;
        this.currentScore = Math.max(0, currentScore);
        this.solvedPuzzleIds = solvedPuzzleIds == null
                ? new HashSet<>()
                : new HashSet<>(solvedPuzzleIds);
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

    public Set<UUID> getSolvedPuzzleIds() {
        return Collections.unmodifiableSet(solvedPuzzleIds);
    }

    public void setSolvedPuzzleIds(Collection<UUID> puzzleIds) {
        solvedPuzzleIds.clear();
        if (puzzleIds != null) {
            solvedPuzzleIds.addAll(puzzleIds);
        }
    }

    public void markPuzzleSolved(UUID puzzleId) {
        if (puzzleId != null) {
            solvedPuzzleIds.add(puzzleId);
        }
    }

    public void clearSolvedPuzzles() {
        solvedPuzzleIds.clear();
    }
}
