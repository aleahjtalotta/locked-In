package com.classes;

import java.time.Duration;
import java.util.Objects;

/**
 * Mutable snapshot of a player's long-term performance. It records how many
 * games they have attempted, victories earned, puzzles solved, and their
 * rolling average completion time, normalizing negative inputs to zero so the
 * values stay meaningful when persisted or displayed.
 */
public class Statistics {
    private int gamesPlayed;
    private int puzzlesSolved;
    private Duration averageCompletionTime;
    private int gamesWon;

    public Statistics() {
        this(0, 0, Duration.ZERO, 0);
    }

    public Statistics(int gamesPlayed, int puzzlesSolved, Duration averageCompletionTime, int gamesWon) {
        this.gamesPlayed = Math.max(0, gamesPlayed);
        this.puzzlesSolved = Math.max(0, puzzlesSolved);
        this.averageCompletionTime = averageCompletionTime == null ? Duration.ZERO : averageCompletionTime;
        this.gamesWon = Math.max(0, gamesWon);
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = Math.max(0, gamesPlayed);
    }

    public int getPuzzlesSolved() {
        return puzzlesSolved;
    }

    public void setPuzzlesSolved(int puzzlesSolved) {
        this.puzzlesSolved = Math.max(0, puzzlesSolved);
    }

    public Duration getAverageCompletionTime() {
        return averageCompletionTime;
    }

    public void setAverageCompletionTime(Duration averageCompletionTime) {
        this.averageCompletionTime = Objects.requireNonNullElse(averageCompletionTime, Duration.ZERO);
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = Math.max(0, gamesWon);
    }

    public void registerGame(boolean won, Duration completionTime, int puzzlesSolvedInGame) {
        gamesPlayed++;
        if (won) {
            gamesWon++;
        }
        if (completionTime != null && !completionTime.isZero()) {
            if (gamesPlayed == 1) {
                averageCompletionTime = completionTime;
            } else {
                long totalSeconds = averageCompletionTime.getSeconds() * (gamesPlayed - 1) + completionTime.getSeconds();
                averageCompletionTime = Duration.ofSeconds(totalSeconds / gamesPlayed);
            }
        }
        puzzlesSolved += Math.max(0, puzzlesSolvedInGame);
    }
}
