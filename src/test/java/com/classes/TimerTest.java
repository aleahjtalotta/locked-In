package com.classes;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

public class TimerTest {

    @Test
    public void setTotalTimeResetsRemainingWhenStopped() {
        Timer timer = new Timer();

        timer.setTotalTime(Duration.ofMinutes(1));

        assertEquals(Duration.ofMinutes(1), timer.getTotalTime());
        assertEquals(Duration.ofMinutes(1), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
        assertFalse(timer.isRunning());
    }

    @Test
    public void startAndPauseCaptureElapsedTime() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(200));

        timer.start();
        Thread.sleep(20);
        assertTrue(timer.isRunning());

        timer.pause();

        assertFalse(timer.isRunning());
        Duration remaining = timer.getRemaining();
        Duration elapsed = timer.getElapsed();
        assertTrue(remaining.compareTo(timer.getTotalTime()) <= 0);
        assertTrue(elapsed.compareTo(Duration.ZERO) > 0);
    }

    @Test
    public void resetRestoresTotalTimeAndClearsElapsed() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(200));
        timer.start();
        Thread.sleep(10);
        timer.pause();

        timer.reset();

        assertFalse(timer.isRunning());
        assertEquals(timer.getTotalTime(), timer.getRemaining());
        assertEquals(Duration.ZERO, timer.getElapsed());
    }

    @Test
    public void setRemainingAdjustsElapsedWhenTotalKnown() {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofSeconds(30));

        timer.setRemaining(Duration.ofSeconds(10));

        assertEquals(Duration.ofSeconds(10), timer.getRemaining());
        assertEquals(Duration.ofSeconds(20), timer.getElapsed());
    }

    @Test
    public void getElapsedIncludesRunningTime() throws InterruptedException {
        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofMillis(200));

        timer.start();
        Thread.sleep(15);

        assertTrue(timer.getElapsed().compareTo(Duration.ZERO) > 0);
        assertTrue(timer.getRemaining().compareTo(timer.getTotalTime()) < 0);

        timer.pause();
    }
}
