package com.classes;

import org.junit.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScoreEntryTest {

    private ScoreEntry createEntry(String name, int score, Duration time) {
        return new ScoreEntry(
                UUID.randomUUID(),
                3L,
                name,
                score,
                time
        );
    }

    @Test
    public void constructorClampsNegativeScoreToZero() {
        ScoreEntry entry = createEntry("Ada", -50, Duration.ofMinutes(5));

        assertEquals(0, entry.getScore());
    }

    @Test
    public void constructorDefaultsNullNameToUnknown() {
        ScoreEntry entry = createEntry(null, 200, Duration.ofMinutes(5));

        assertEquals("Unknown", entry.getPlayerName());
    }

    @Test
    public void constructorDefaultsNullCompletionTimeToZeroDuration() {
        ScoreEntry entry = createEntry("Ada", 200, null);

        assertEquals(Duration.ZERO, entry.getCompletionTime());
    }

    @Test
    public void compareToPlacesHigherScoresAheadOfLowerScores() {
        ScoreEntry highScore = createEntry("Ada", 200, Duration.ofMinutes(10));
        ScoreEntry lowScore = createEntry("Bea", 150, Duration.ofMinutes(5));

        assertTrue(highScore.compareTo(lowScore) < 0);
    }

    @Test
    public void compareToUsesFasterCompletionWhenScoresMatch() {
        ScoreEntry faster = createEntry("Ada", 200, Duration.ofMinutes(5));
        ScoreEntry slower = createEntry("Bea", 200, Duration.ofMinutes(10));

        assertTrue(faster.compareTo(slower) < 0);
    }
}
