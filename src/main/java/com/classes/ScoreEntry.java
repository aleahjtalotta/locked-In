package com.classes;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable leaderboard record that ranks players by score (higher is better)
 * and uses completion time as a tie breaker. Instances may originate either
 * from legacy numeric identifiers or freshly generated UUIDs, but the
 * comparison contract always orders the best-performing players first.
 */
public class ScoreEntry implements Comparable<ScoreEntry> {
    private final UUID id;
    private final Long legacyId;
    private final String playerName;
    private final int score;
    private final Duration completionTime;

    /**
     * Constructs a leaderboard entry.
     *
     * @param id              unique identifier for the entry; must not be {@code null}
     * @param legacyId        optional legacy numeric identifier; may be {@code null}
     * @param playerName      display name of the player; {@code null} defaults to {@code "Unknown"}
     * @param score           score awarded to the player; negative values are clamped to zero
     * @param completionTime  time taken to finish; {@code null} becomes {@link Duration#ZERO}
     */
    public ScoreEntry(UUID id, Long legacyId, String playerName, int score, Duration completionTime) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.playerName = Objects.requireNonNullElse(playerName, "Unknown");
        this.score = Math.max(0, score);
        this.completionTime = completionTime == null ? Duration.ZERO : completionTime;
    }

    /**
     * @return immutable identifier for this entry
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return optional legacy numeric identifier, or {@code null} when absent
     */
    public Long getLegacyId() {
        return legacyId;
    }

    /**
     * @return player name associated with this entry
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return score awarded to the player for this entry
     */
    public int getScore() {
        return score;
    }

    /**
     * @return completion time recorded for the player
     */
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
