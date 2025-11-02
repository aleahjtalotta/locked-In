package com.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

/**
 * Unit tests for {@link Timer} covering both typical and error scenarios.
 */
public class TimerTest {

    @Test
    public void setTotalTime_whenStopped_resetsRemainingAndElapsed() {
        Timer timer = new Timer();

        timer.setTotalTime(Duration.ofMinutes(5));

        assertEquals(Duration.ofMinutes(5), timer.getTotalTime());
        assertEquals(Duration.ofMinutes(5), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
    }

    @Test
    public void start_setsRunningSinceAndIsRunning() {
        Timer timer = new Timer();

        timer.start();

        assertTrue(timer.isRunning());
        assertTrue(timer.getRunningSince().isPresent());
    }

    @Test
    public void start_whenAlreadyRunning_keepsOriginalStartInstant() {
        Timer timer = new Timer();

        timer.start();
        Instant firstStart = timer.getRunningSince().orElseThrow();
        sleepMillis(25);

        timer.start();

        assertTrue(timer.getRunningSince().isPresent());
        assertEquals(firstStart, timer.getRunningSince().orElseThrow());
    }

    @Test
    public void pause_whenRunning_updatesRemainingAndStopsTimer() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(300));

        timer.start();
        sleepMillis(60);
        timer.pause();

        Duration remaining = timer.getRemaining();
        assertFalse(timer.isRunning());
        assertTrue(remaining.compareTo(Duration.ZERO) > 0);
        assertTrue(remaining.compareTo(Duration.ofMillis(300)) < 0);
        assertFalse(timer.getRunningSince().isPresent());
    }

    @Test
    public void reset_restoresRemainingAndClearsElapsed() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(2));
        timer.start();
        sleepMillis(75);

        timer.reset();

        assertFalse(timer.isRunning());
        assertEquals(Duration.ofSeconds(2), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
    }

    @Test
    public void setRemaining_alignsElapsedWithTotalTime() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(10));

        timer.setRemaining(Duration.ofSeconds(4));

        assertEquals(Duration.ofSeconds(4), timer.getRemaining());
        assertEquals(Duration.ofSeconds(6), timer.getElapsed());
    }

    @Test
    public void setRemaining_nullClearsRemainingAndCapturesElapsed() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(10));

        timer.setRemaining(null);

        assertEquals(Duration.ZERO, timer.getRemaining());
        assertEquals(Duration.ofSeconds(10), timer.getElapsed());
    }

    @Test
    public void getElapsed_includesCurrentRunTime() {
        Timer timer = new Timer();

        timer.start();
        sleepMillis(40);

        assertTrue(timer.getElapsed().compareTo(Duration.ZERO) > 0);
    }

    @Test
    public void setTotalTime_whileRunning_shouldUpdateRemaining() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(2));

        timer.start();
        sleepMillis(120);
        timer.setTotalTime(Duration.ofSeconds(1));
        Duration remaining = timer.getRemaining();

        assertTrue("Remaining time should not exceed the new total",
                remaining.compareTo(Duration.ofSeconds(1)) <= 0);
    }

    @Test
    public void setRemaining_whileRunning_shouldHonorNewValue() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(5));

        timer.start();
        sleepMillis(120);
        timer.setRemaining(Duration.ofSeconds(4));
        Duration remaining = timer.getRemaining();
        long difference = Math.abs(remaining.toMillis() - 4000);

        assertTrue("Remaining time should stay close to the requested value", difference <= 50);
    }

    @Test
    public void setRemaining_negativeDuration_shouldClampToZero() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(3));

        timer.setRemaining(Duration.ofSeconds(-2));

        assertEquals(Duration.ZERO, timer.getRemaining());
    }

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
