package com.classes;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single entry on the leaderboard.
 */
public class ScoreEntry implements Comparable<ScoreEntry> {
    private final UUID id;
    private final Long legacyId;
    private final String playerName;
    private final int score;
    private final Duration completionTime;

    public ScoreEntry(UUID id, Long legacyId, String playerName, int score, Duration completionTime) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.playerName = Objects.requireNonNullElse(playerName, "Unknown");
        this.score = Math.max(0, score);
        this.completionTime = completionTime == null ? Duration.ZERO : completionTime;
    }

    public UUID getId() {
        return id;
    }

    public Long getLegacyId() {
        return legacyId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public Duration getCompletionTime() {
        return completionTime;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        int scoreComparison = Integer.compare(other.score, score);
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        return completionTime.compareTo(other.completionTime);
    }
}
