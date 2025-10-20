package com.classes;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Simple countdown timer abstraction.
 */
public class Timer {
    private Duration totalTime = Duration.ZERO;
    private Duration remaining = Duration.ZERO;
    private Instant runningSince;

    public Duration getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Duration totalTime) {
        this.totalTime = totalTime == null ? Duration.ZERO : totalTime;
        if (!isRunning()) {
            remaining = this.totalTime;
        }
    }

    public Duration getRemaining() {
        if (isRunning()) {
            Duration elapsed = Duration.between(runningSince, Instant.now());
            return remaining.minus(elapsed).isNegative() ? Duration.ZERO : remaining.minus(elapsed);
        }
        return remaining;
    }

    public void start() {
        if (!isRunning()) {
            runningSince = Instant.now();
        }
    }

    public void pause() {
        if (isRunning()) {
            remaining = getRemaining();
            runningSince = null;
        }
    }

    public void reset() {
        runningSince = null;
        remaining = totalTime;
    }

    public boolean isRunning() {
        return runningSince != null;
    }

    public Optional<Instant> getRunningSince() {
        return Optional.ofNullable(runningSince);
    }

    public void setRemaining(Duration remaining) {
        this.remaining = remaining == null ? Duration.ZERO : remaining;
    }
}
