package com.classes;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class StatisticsTest {

    @Test
    public void constructorClampsInvalidValues() {
        Statistics statistics = new Statistics(-5, -2, null, -7);

        assertEquals(0, statistics.getGamesPlayed());
        assertEquals(0, statistics.getPuzzlesSolved());
        assertEquals(Duration.ZERO, statistics.getAverageCompletionTime());
        assertEquals(0, statistics.getGamesWon());
    }

    @Test
    public void settersClampNegativeValues() {
        Statistics statistics = new Statistics();

        statistics.setGamesPlayed(-1);
        statistics.setPuzzlesSolved(-4);
        statistics.setGamesWon(-3);
        statistics.setAverageCompletionTime(null);

        assertEquals(0, statistics.getGamesPlayed());
        assertEquals(0, statistics.getPuzzlesSolved());
        assertEquals(0, statistics.getGamesWon());
        assertEquals(Duration.ZERO, statistics.getAverageCompletionTime());
    }

    @Test
    public void registerGameAccumulatesCountsAndAverage() {
        Statistics statistics = new Statistics();

        statistics.registerGame(true, Duration.ofMinutes(10), 3);
        statistics.registerGame(false, Duration.ofMinutes(20), 2);
        statistics.registerGame(true, null, -5);

        assertEquals(3, statistics.getGamesPlayed());
        assertEquals(2, statistics.getGamesWon());
        assertEquals(5, statistics.getPuzzlesSolved());
        assertEquals(Duration.ofMinutes(15), statistics.getAverageCompletionTime());
    }

    @Test
    public void registerGameClampsNegativeCompletionTime() {
        Statistics statistics = new Statistics();

        statistics.registerGame(false, Duration.ofSeconds(-30), 0);

        assertEquals("Negative durations should be normalized to zero", Duration.ZERO, statistics.getAverageCompletionTime());
    }
}
