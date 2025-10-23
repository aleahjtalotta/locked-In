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
    private Duration elapsedAccumulated = Duration.ZERO;

    public Duration getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Duration totalTime) {
        this.totalTime = totalTime == null ? Duration.ZERO : totalTime;
        if (!isRunning()) {
            remaining = this.totalTime;
            elapsedAccumulated = Duration.ZERO;
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
            Duration elapsedSinceStart = Duration.between(runningSince, Instant.now());
            elapsedAccumulated = elapsedAccumulated.plus(elapsedSinceStart);
            remaining = getRemaining();
            runningSince = null;
        }
    }

    public void reset() {
        runningSince = null;
        remaining = totalTime;
        elapsedAccumulated = Duration.ZERO;
    }

    public boolean isRunning() {
        return runningSince != null;
    }

    public Optional<Instant> getRunningSince() {
        return Optional.ofNullable(runningSince);
    }

    public void setRemaining(Duration remaining) {
        this.remaining = remaining == null ? Duration.ZERO : remaining;
        if (!totalTime.isZero()) {
            Duration derivedElapsed = totalTime.minus(this.remaining);
            if (!derivedElapsed.isNegative()) {
                elapsedAccumulated = derivedElapsed;
            }
        }
    }

    public Duration getElapsed() {
        Duration elapsed = elapsedAccumulated;
        if (isRunning()) {
            elapsed = elapsed.plus(Duration.between(runningSince, Instant.now()));
        }
        if (!totalTime.isZero()) {
            Duration derived = totalTime.minus(getRemaining());
            if (!derived.isNegative()) {
                return derived;
            }
        }
        return elapsed.isNegative() ? Duration.ZERO : elapsed;
    }
}
