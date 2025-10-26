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

    /**
     * Creates a statistics snapshot with explicit values.
     *
     * @param gamesPlayed total number of games attempted; negatives are clamped to zero
     * @param puzzlesSolved cumulative puzzles completed; negatives are clamped to zero
     * @param averageCompletionTime rolling average completion time; {@code null} becomes {@link Duration#ZERO}
     * @param gamesWon total wins recorded; negatives are clamped to zero
     */
    public Statistics(int gamesPlayed, int puzzlesSolved, Duration averageCompletionTime, int gamesWon) {
        this.gamesPlayed = Math.max(0, gamesPlayed);
        this.puzzlesSolved = Math.max(0, puzzlesSolved);
        this.averageCompletionTime = averageCompletionTime == null ? Duration.ZERO : averageCompletionTime;
        this.gamesWon = Math.max(0, gamesWon);
    }

    /**
     * @return total number of games the player has started
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * Updates how many games the player has attempted.
     *
     * @param gamesPlayed new total; negatives are clamped to zero
     */
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = Math.max(0, gamesPlayed);
    }

    /**
     * @return cumulative puzzles solved across all sessions
     */
    public int getPuzzlesSolved() {
        return puzzlesSolved;
    }

    /**
     * Records the total number of puzzles the player has solved.
     *
     * @param puzzlesSolved new total; negatives are clamped to zero
     */
    public void setPuzzlesSolved(int puzzlesSolved) {
        this.puzzlesSolved = Math.max(0, puzzlesSolved);
    }

    /**
     * @return rolling average time the player takes to finish a game
     */
    public Duration getAverageCompletionTime() {
        return averageCompletionTime;
    }

    /**
     * Sets the rolling average completion time.
     *
     * @param averageCompletionTime updated average; {@code null} becomes {@link Duration#ZERO}
     */
    public void setAverageCompletionTime(Duration averageCompletionTime) {
        this.averageCompletionTime = Objects.requireNonNullElse(averageCompletionTime, Duration.ZERO);
    }

    /**
     * @return count of games the player has won
     */
    public int getGamesWon() {
        return gamesWon;
    }

    /**
     * Updates the number of games the player has won.
     *
     * @param gamesWon new total; negatives are clamped to zero
     */
    public void setGamesWon(int gamesWon) {
        this.gamesWon = Math.max(0, gamesWon);
    }

    /**
     * Incorporates the latest game result into this statistics object.
     *
     * @param won                  {@code true} if the player won the game
     * @param completionTime       time taken to finish the game; ignored when {@code null} or zero
     * @param puzzlesSolvedInGame  puzzles solved during the game; negatives are treated as zero
     */
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
