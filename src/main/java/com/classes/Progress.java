package com.classes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Captures the player's current run through the escape room. In addition to
 * remembering which player is active and which room they occupy, the progress
 * object holds the set of puzzles that have already been solved so the UI can
 * restore state after a reload.
 */
public class Progress {
    private UUID activePlayerId;
    private UUID currentRoomId;
    private final Set<UUID> solvedPuzzleIds;

    public Progress() {
        this.solvedPuzzleIds = new HashSet<>();
    }

    /**
     * @return identifier of the player whose progress is captured
     */
    public UUID getActivePlayerId() {
        return activePlayerId;
    }

    /**
     * Sets the active player identifier tracked by this progress object.
     *
     * @param activePlayerId player identifier to associate; may be {@code null}
     */
    public void setActivePlayerId(UUID activePlayerId) {
        this.activePlayerId = activePlayerId;
    }

    /**
     * @return identifier of the room the player currently occupies, or {@code null}
     */
    public UUID getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * Updates the room the player currently occupies.
     *
     * @param currentRoomId room identifier to track; may be {@code null}
     */
    public void setCurrentRoomId(UUID currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    /**
     * Marks a puzzle as solved in the progress record.
     *
     * @param puzzleId puzzle identifier to record; ignored when {@code null}
     */
    public void markPuzzleSolved(UUID puzzleId) {
        if (puzzleId != null) {
            solvedPuzzleIds.add(puzzleId);
        }
    }

    /**
     * Checks whether the supplied puzzle has been marked as solved.
     *
     * @param puzzleId puzzle identifier to query
     * @return {@code true} when the puzzle has been solved
     */
    public boolean isPuzzleSolved(UUID puzzleId) {
        return puzzleId != null && solvedPuzzleIds.contains(puzzleId);
    }

    /**
     * @return unmodifiable view of all puzzle identifiers marked as solved
     */
    public Set<UUID> getSolvedPuzzleIds() {
        return Collections.unmodifiableSet(solvedPuzzleIds);
    }

    /**
     * Clears the current progress and associates it with a new player.
     *
     * @param playerId identifier of the player starting a fresh run; must not be {@code null}
     */
    public void reset(UUID playerId) {
        this.activePlayerId = Objects.requireNonNull(playerId, "playerId");
        this.currentRoomId = null;
        this.solvedPuzzleIds.clear();
    }

    /**
     * Replaces the solved-puzzle set with the provided identifiers.
     *
     * @param puzzleIds collection of solved puzzle identifiers; {@code null} clears the set
     */
    public void loadSolvedPuzzles(Collection<UUID> puzzleIds) {
        this.solvedPuzzleIds.clear();
        if (puzzleIds != null) {
            this.solvedPuzzleIds.addAll(puzzleIds);
        }
    }

    /**
     * Removes every puzzle from the solved set.
     */
    public void clearSolved() {
        this.solvedPuzzleIds.clear();
    }
}
