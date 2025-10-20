package com.classes;

import java.time.Duration;
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

    public void addScoreEntry(UUID id, Long legacyId, String playerName, Duration completionTime, int score) {
        addScoreEntry(new ScoreEntry(id, legacyId, playerName, score, completionTime));
    }

    public void addScoreEntry(UUID id, String playerName, int score, Duration completionTime) {
        addScoreEntry(new ScoreEntry(id, null, playerName, score, completionTime));
    }

    public List<ScoreEntry> getScores() {
        return Collections.unmodifiableList(scores);
    }

    public Optional<ScoreEntry> getTopScore() {
        return scores.isEmpty() ? Optional.empty() : Optional.of(scores.get(0));
    }

    public void updateLeaderboard(Player player, int score) {
        updateLeaderboard(player, score, Duration.ZERO);
    }

    public void updateLeaderboard(Player player, int score, Duration completionTime) {
        if (player == null) {
            return;
        }
        String name = player.getName();
        Duration safeDuration = completionTime == null ? Duration.ZERO : completionTime;
        Optional<ScoreEntry> existing = scores.stream()
                .filter(entry -> entry.getPlayerName().equalsIgnoreCase(name))
                .findFirst();
        UUID entryId = UUID.nameUUIDFromBytes(("score-" + player.getId()).getBytes());

        if (existing.isPresent()) {
            if (score > existing.get().getScore()) {
                scores.remove(existing.get());
                addScoreEntry(new ScoreEntry(entryId, null, name, score, safeDuration));
            }
        } else {
            addScoreEntry(new ScoreEntry(entryId, null, name, score, safeDuration));
        }
    }

    public void displayTopPlayer() {
        getTopScore().ifPresentOrElse(
                top -> System.out.println("TOP PLAYER: " + top.getPlayerName() + " - " + top.getScore()),
                () -> System.out.println("No scores available yet!")
        );
    }

    public void displayPlayerScore(Player player) {
        if (player == null) {
            System.out.println("No player selected.");
            return;
        }
        scores.stream()
                .filter(entry -> entry.getPlayerName().equalsIgnoreCase(player.getName()))
                .findFirst()
                .ifPresentOrElse(
                        entry -> System.out.println(player.getName() + "'s score: " + entry.getScore()),
                        () -> System.out.println(player.getName() + " has no score on the leaderboard.")
                );
    }
}
