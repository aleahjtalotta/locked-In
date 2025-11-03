package com.classes;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LeaderboardTest {

    private ScoreEntry score(String name, int score, Duration duration) {
        return new ScoreEntry(UUID.randomUUID(), null, name, score, duration);
    }

    private Player player(String name) {
        return new Player(
                UUID.randomUUID(),
                1,
                name,
                name.toLowerCase() + "@example.com",
                null,
                new ItemList(),
                new Statistics(),
                0,
                null
        );
    }

    @Test
    public void addScoreEntrySortsScoresDescending() {
        Leaderboard leaderboard = new Leaderboard();
        ScoreEntry low = score("Low", 50, Duration.ofMinutes(30));
        ScoreEntry high = score("High", 100, Duration.ofMinutes(20));
        leaderboard.addScoreEntry(low);
        leaderboard.addScoreEntry(high);
        List<ScoreEntry> scores = leaderboard.getScores();
        assertSame(high, scores.get(0));
        assertSame(low, scores.get(1));
    }

    @Test
    public void addScoreEntryIgnoresNullInput() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.addScoreEntry((ScoreEntry) null);
        assertTrue(leaderboard.getScores().isEmpty());
    }

    @Test
    public void addScoreEntryOverloadCreatesEntryWithLegacyId() {
        Leaderboard leaderboard = new Leaderboard();
        UUID id = UUID.randomUUID();
        leaderboard.addScoreEntry(id, 5L, "Ada", Duration.ofMinutes(12), 200);
        ScoreEntry entry = leaderboard.getScores().get(0);
        assertEquals(Long.valueOf(5), entry.getLegacyId());
        assertEquals("Ada", entry.getPlayerName());
        assertEquals(200, entry.getScore());
        assertEquals(Duration.ofMinutes(12), entry.getCompletionTime());
    }

    @Test
    public void addScoreEntryOverloadWithoutLegacyIdDefaultsValues() {
        Leaderboard leaderboard = new Leaderboard();
        UUID id = UUID.randomUUID();
        leaderboard.addScoreEntry(id, "Ada", 180, Duration.ofMinutes(15));
        ScoreEntry entry = leaderboard.getScores().get(0);
        assertNull(entry.getLegacyId());
        assertEquals("Ada", entry.getPlayerName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getScoresIsUnmodifiableView() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.addScoreEntry(score("Ada", 150, Duration.ofMinutes(10)));
        leaderboard.getScores().add(score("Grace", 120, Duration.ofMinutes(15)));
    }

    @Test
    public void getTopScoreReturnsHighestEntryWhenPresent() {
        Leaderboard leaderboard = new Leaderboard();
        ScoreEntry high = score("High", 200, Duration.ofMinutes(5));
        ScoreEntry low = score("Low", 150, Duration.ofMinutes(3));
        leaderboard.addScoreEntry(low);
        leaderboard.addScoreEntry(high);
        Optional<ScoreEntry> top = leaderboard.getTopScore();
        assertTrue(top.isPresent());
        assertSame(high, top.get());
    }

    @Test
    public void getTopScoreReturnsEmptyWhenNoEntries() {
        Leaderboard leaderboard = new Leaderboard();
        assertTrue(leaderboard.getTopScore().isEmpty());
    }

    @Test
    public void updateLeaderboardAddsEntryWhenPlayerMissing() {
        Leaderboard leaderboard = new Leaderboard();
        Player player = player("Ada");
        leaderboard.updateLeaderboard(player, 250);
        List<ScoreEntry> scores = leaderboard.getScores();
        assertEquals(1, scores.size());
        assertEquals("Ada", scores.get(0).getPlayerName());
        assertEquals(250, scores.get(0).getScore());
    }

    @Test
    public void updateLeaderboardReplacesEntryWhenScoreImproves() {
        Leaderboard leaderboard = new Leaderboard();
        Player player = player("Ada");
        leaderboard.updateLeaderboard(player, 200, Duration.ofMinutes(12));
        leaderboard.updateLeaderboard(player, 300, Duration.ofMinutes(10));
        List<ScoreEntry> scores = leaderboard.getScores();
        assertEquals(1, scores.size());
        assertEquals(300, scores.get(0).getScore());
        assertEquals(Duration.ofMinutes(10), scores.get(0).getCompletionTime());
    }

    @Test
    public void updateLeaderboardKeepsExistingWhenNewScoreLower() {
        Leaderboard leaderboard = new Leaderboard();
        Player player = player("Ada");
        leaderboard.updateLeaderboard(player, 300, Duration.ofMinutes(10));
        leaderboard.updateLeaderboard(player, 150, Duration.ofMinutes(8));
        List<ScoreEntry> scores = leaderboard.getScores();
        assertEquals(1, scores.size());
        assertEquals(300, scores.get(0).getScore());
    }

    @Test
    public void updateLeaderboardIgnoresNullPlayer() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.updateLeaderboard(null, 200, Duration.ofMinutes(10));
        assertTrue(leaderboard.getScores().isEmpty());
    }

    @Test
    public void updateLeaderboardDefaultsNullDurationToZero() {
        Leaderboard leaderboard = new Leaderboard();
        Player player = player("Ada");
        leaderboard.updateLeaderboard(player, 220, null);
        assertEquals(Duration.ZERO, leaderboard.getScores().get(0).getCompletionTime());
    }

    @Test
    public void displayTopPlayerPrintsHighScore() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.addScoreEntry(score("Ada", 250, Duration.ofMinutes(5)));
        String output = captureOutput(leaderboard::displayTopPlayer);
        assertTrue(output.contains("TOP PLAYER: Ada - 250"));
    }

    @Test
    public void displayTopPlayerPrintsFallbackWhenEmpty() {
        Leaderboard leaderboard = new Leaderboard();
        String output = captureOutput(leaderboard::displayTopPlayer);
        assertTrue(output.contains("No scores available yet!"));
    }

    @Test
    public void displayPlayerScorePrintsScoreWhenFound() {
        Leaderboard leaderboard = new Leaderboard();
        Player player = player("Ada");
        leaderboard.updateLeaderboard(player, 275, Duration.ofMinutes(7));
        String output = captureOutput(() -> leaderboard.displayPlayerScore(player));
        assertTrue(output.contains("Ada's score: 275"));
    }

    @Test
    public void displayPlayerScoreReportsMissingWhenNotOnBoard() {
        Leaderboard leaderboard = new Leaderboard();
        Player player = player("Ada");
        String output = captureOutput(() -> leaderboard.displayPlayerScore(player));
        assertTrue(output.contains("Ada has no score on the leaderboard."));
    }

    @Test
    public void displayPlayerScoreWarnsWhenPlayerNull() {
        Leaderboard leaderboard = new Leaderboard();
        String output = captureOutput(() -> leaderboard.displayPlayerScore(null));
        assertTrue(output.contains("No player selected."));
    }

    private String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream capture = new PrintStream(buffer)) {
            System.setOut(capture);
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
