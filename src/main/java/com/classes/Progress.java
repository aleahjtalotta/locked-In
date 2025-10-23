package com.classes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks the current state of a play-through.
 */
public class Progress {
    private UUID activePlayerId;
    private UUID currentRoomId;
    private final Set<UUID> solvedPuzzleIds;

    public Progress() {
        this.solvedPuzzleIds = new HashSet<>();
    }

    public UUID getActivePlayerId() {
        return activePlayerId;
    }

    public void setActivePlayerId(UUID activePlayerId) {
        this.activePlayerId = activePlayerId;
    }

    public UUID getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(UUID currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public void markPuzzleSolved(UUID puzzleId) {
        if (puzzleId != null) {
            solvedPuzzleIds.add(puzzleId);
        }
    }

    public boolean isPuzzleSolved(UUID puzzleId) {
        return puzzleId != null && solvedPuzzleIds.contains(puzzleId);
    }

    public Set<UUID> getSolvedPuzzleIds() {
        return Collections.unmodifiableSet(solvedPuzzleIds);
    }

    public void reset(UUID playerId) {
        this.activePlayerId = Objects.requireNonNull(playerId, "playerId");
        this.currentRoomId = null;
        this.solvedPuzzleIds.clear();
    }

    public void loadSolvedPuzzles(Collection<UUID> puzzleIds) {
        this.solvedPuzzleIds.clear();
        if (puzzleIds != null) {
            this.solvedPuzzleIds.addAll(puzzleIds);
        }
    }

    public void clearSolved() {
        this.solvedPuzzleIds.clear();
    }
}
