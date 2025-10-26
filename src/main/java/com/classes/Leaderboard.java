package com.classes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Small helper that keeps track of top scores for the escape room.
 * I wrote the comments the way I would explain it to a classmate so it stays simple.
 */
public class Leaderboard {
    private final List<ScoreEntry> scores;

    /**
     * Makes a new leaderboard with nothing in it yet.
     */
    public Leaderboard() {
        this.scores = new ArrayList<>();
    }

    /**
     * Adds a score entry object and keeps the list sorted.
     *
     * @param entry score info to drop onto the board; ignored when null
     */
    public void addScoreEntry(ScoreEntry entry) {
        if (entry != null) {
            scores.add(entry);
            scores.sort(null);
        }
    }

    /**
     * Builds a score entry from raw data and adds it.
     *
     * @param id unique id for this entry
     * @param legacyId optional id from an older system
     * @param playerName name we show on the board
     * @param completionTime how long they took to finish
     * @param score total points earned
     */
    public void addScoreEntry(UUID id, Long legacyId, String playerName, Duration completionTime, int score) {
        addScoreEntry(new ScoreEntry(id, legacyId, playerName, score, completionTime));
    }

    /**
     * Quick helper when we do not care about the legacy id.
     *
     * @param id unique id for the entry
     * @param playerName name to show
     * @param score points earned
     * @param completionTime run time for the attempt
     */
    public void addScoreEntry(UUID id, String playerName, int score, Duration completionTime) {
        addScoreEntry(new ScoreEntry(id, null, playerName, score, completionTime));
    }

    /**
     * Lets callers see the scores without letting them change the list.
     *
     * @return read-only view of every score we are storing
     */
    public List<ScoreEntry> getScores() {
        return Collections.unmodifiableList(scores);
    }

    /**
     * Grabs the top score if one exists.
     *
     * @return optional with the best score or empty when nobody played yet
     */
    public Optional<ScoreEntry> getTopScore() {
        return scores.isEmpty() ? Optional.empty() : Optional.of(scores.get(0));
    }

    /**
     * Updates the board for a player using a default completion time.
     *
     * @param player player to track
     * @param score new score we want to store
     */
    public void updateLeaderboard(Player player, int score) {
        updateLeaderboard(player, score, Duration.ZERO);
    }

    /**
     * Puts a player's score on the board and only keeps their best run.
     *
     * @param player player info; ignored when null
     * @param score score to try to post
     * @param completionTime time they took; replaces null with zero duration
     */
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

    /**
     * Prints the highest scoring player or a friendly reminder if nothing is recorded.
     */
    public void displayTopPlayer() {
        getTopScore().ifPresentOrElse(
                top -> System.out.println("TOP PLAYER: " + top.getPlayerName() + " - " + top.getScore()),
                () -> System.out.println("No scores available yet!")
        );
    }

    /**
     * Prints either the player's score or a note saying they do not have one.
     *
     * @param player person we are looking up; prints a warning when null
     */
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
