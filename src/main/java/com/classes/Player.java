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

    /**
     * Creates a player profile with the supplied metadata, inventory, and prior progress.
     *
     * @param id              unique identifier for the player
     * @param legacyId        optional legacy numeric identifier
     * @param name            display name for the player
     * @param email           contact email used as a unique login
     * @param avatar          optional avatar asset reference
     * @param inventory       items held by the player, or {@code null} for an empty list
     * @param statistics      cumulative statistics tracked for the player, or {@code null} for defaults
     * @param currentScore    current score accumulated during play
     * @param solvedPuzzleIds collection of puzzles already solved by the player
     */
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

    /**
     * Retrieves the immutable primary identifier for the player.
     *
     * @return globally unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Provides the legacy numeric identifier when available.
     *
     * @return legacy identifier or {@code null} if none exists
     */
    public Integer getLegacyId() {
        return legacyId;
    }

    /**
     * Returns the player's display name.
     *
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the player's display name, ignoring {@code null} values.
     *
     * @param name new player name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, this.name);
    }

    /**
     * Returns the player's email address.
     *
     * @return email address, possibly empty
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the player's contact email, ignoring {@code null} values.
     *
     * @param email new email address
     */
    public void setEmail(String email) {
        this.email = Objects.requireNonNullElse(email, this.email);
    }

    /**
     * Retrieves the avatar reference for the player.
     *
     * @return avatar identifier or {@code null}
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Assigns a new avatar reference.
     *
     * @param avatar avatar identifier, or {@code null} to clear
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Provides access to the player's inventory list.
     *
     * @return modifiable inventory list owned by this player
     */
    public ItemList getInventory() {
        return inventory;
    }

    /**
     * Returns tracked statistics for the player.
     *
     * @return statistics collection
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Reports the current accumulated score.
     *
     * @return non-negative score value
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /**
     * Adjusts the player's score by the provided delta, clamping the result at zero.
     *
     * @param delta score change to apply
     */
    public void addScore(int delta) {
        currentScore = Math.max(0, currentScore + delta);
    }

    /**
     * Returns an immutable view of the puzzle identifiers already solved by the player.
     *
     * @return unmodifiable set of puzzle ids
     */
    public Set<UUID> getSolvedPuzzleIds() {
        return Collections.unmodifiableSet(solvedPuzzleIds);
    }

    /**
     * Replaces the solved puzzle history with the supplied set of identifiers.
     *
     * @param puzzleIds new collection of solved puzzle ids
     */
    public void setSolvedPuzzleIds(Collection<UUID> puzzleIds) {
        solvedPuzzleIds.clear();
        if (puzzleIds != null) {
            solvedPuzzleIds.addAll(puzzleIds);
        }
    }

    /**
     * Records that the player has solved the puzzle with the given identifier.
     *
     * @param puzzleId puzzle identifier to record
     */
    public void markPuzzleSolved(UUID puzzleId) {
        if (puzzleId != null) {
            solvedPuzzleIds.add(puzzleId);
        }
    }

    /**
     * Removes all solved puzzle records for the player.
     */
    public void clearSolvedPuzzles() {
        solvedPuzzleIds.clear();
    }
}
