package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Leaderboard {
    private final List<ScoreEntry> scores;

    public Leaderboard() {
        this.scores = new ArrayList<>();
    }

    public void addScoreEntry(ScoreEntry entry) {
        if (entry != null) {
            scores.add(entry);
            scores.sort(null);
        }
    }

    public void addScoreEntry(UUID id, Long legacyId, String playerName, java.time.Duration completionTime, int score) {
        addScoreEntry(new ScoreEntry(id, legacyId, playerName, score, completionTime));
    }

    public void addScoreEntry(UUID id, String playerName, int score, java.time.Duration completionTime) {
        addScoreEntry(new ScoreEntry(id, null, playerName, score, completionTime));
    }

    public List<ScoreEntry> getScores() {
        return Collections.unmodifiableList(scores);
    }

    public Optional<ScoreEntry> getTopScore() {
        return scores.isEmpty() ? Optional.empty() : Optional.of(scores.get(0));
    }
}
