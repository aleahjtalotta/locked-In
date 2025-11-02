package com.classes;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;

public class TimerTest {

    private static final Duration TOLERANCE = Duration.ofMillis(40);

    @Test
    public void setTotalTimeWhenIdleCopiesValueToRemaining() {
        Timer timer = new Timer();

        timer.setTotalTime(Duration.ofSeconds(90));

        assertEquals(Duration.ofSeconds(90), timer.getTotalTime());
        assertEquals(Duration.ofSeconds(90), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
        assertFalse(timer.isRunning());
    }

    @Test
    public void setTotalTimeWithNullResetsCountdown() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(5));

        timer.setTotalTime(null);

        assertEquals(Duration.ZERO, timer.getTotalTime());
        assertEquals(Duration.ZERO, timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
    }

    @Test
    public void startTwiceRetainsOriginalStartInstant() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(5));

        timer.start();
        Instant firstStart = timer.getRunningSince().orElseThrow();
        Thread.sleep(10);

        timer.start();

        Instant secondStart = timer.getRunningSince().orElseThrow();
        assertEquals(firstStart, secondStart);
        timer.pause();
    }

    @Test
    public void pauseAfterRunningPersistsElapsedTime() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(200));

        timer.start();
        Thread.sleep(30);

        timer.pause();

        assertFalse(timer.isRunning());
        assertDurationApproximately(timer.getElapsed(),
                timer.getTotalTime().minus(timer.getRemaining()),
                TOLERANCE);
        assertTrue(timer.getElapsed().compareTo(Duration.ZERO) > 0);
    }

    @Test
    public void resetRestoresInitialState() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(500));
        timer.start();
        Thread.sleep(15);
        timer.pause();

        timer.reset();

        assertFalse(timer.isRunning());
        assertEquals(timer.getTotalTime(), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
        assertFalse(timer.getRunningSince().isPresent());
    }

    @Test
    public void setRemainingNullSetsRemainingToZeroAndAdjustsElapsed() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(10));

        timer.setRemaining(null);

        assertEquals(Duration.ZERO, timer.getRemaining());
        assertEquals(Duration.ofSeconds(10), timer.getElapsed());
    }

    @Test
    public void setRemainingGreaterThanTotalDoesNotForceElapsedNegative() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(1));

        timer.setRemaining(Duration.ofSeconds(2));

        assertEquals(Duration.ofSeconds(2), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
    }

    @Test
    public void getRemainingNeverReturnsNegativeDuration() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(40));

        timer.start();
        Thread.sleep(60);

        assertEquals(Duration.ZERO, timer.getRemaining());
        timer.pause();
        assertEquals(timer.getTotalTime(), timer.getElapsed());
    }

    @Test
    public void getElapsedWithZeroTotalUsesAccumulatedTime() throws InterruptedException {
        Timer timer = new Timer();

        timer.start();
        Thread.sleep(15);

        Duration elapsedWhileRunning = timer.getElapsed();
        assertTrue(elapsedWhileRunning.compareTo(Duration.ZERO) > 0);

        timer.pause();
        Duration elapsedAfterPause = timer.getElapsed();
        assertDurationApproximately(elapsedAfterPause, elapsedWhileRunning, TOLERANCE);
    }

    private static void assertDurationApproximately(Duration actual, Duration expected, Duration tolerance) {
        Duration difference = actual.minus(expected).abs();
        assertTrue("Expected " + actual + " to be within " + tolerance + " of " + expected,
                difference.compareTo(tolerance) <= 0);
    }
}
