package com.classes;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Countdown helper used by the escape-room game to track time-limited sessions.
 * <p>
 * The timer stores a total countdown duration and keeps track of the remaining
 * and elapsed time across multiple start/pause cycles. Time is measured with
 * {@link Duration} and {@link Instant} so the implementation remains immune to
 * system clock changes. Instances are mutable and not thread-safe.
 * </p>
 *
 * @see DataLoader#parseTimer(org.json.simple.JSONObject)
 * @see DataWriter#writeTimer(Timer)
 */
public class Timer {
    private Duration totalTime = Duration.ZERO;
    private Duration remaining = Duration.ZERO;
    private Instant runningSince;
    private Duration elapsedAccumulated = Duration.ZERO;

    /**
     * Returns the total configured duration for the timer.
     *
     * @return total countdown duration; never {@code null}
     */
    public Duration getTotalTime() {
        return totalTime;
    }

    /**
     * Sets the total duration the timer should run for. When the timer is not
     * currently running this also resets the remaining time to the provided
     * value and clears any elapsed time that had been accumulated.
     *
     * @param totalTime the desired countdown duration; {@code null} resets to zero
     */
    public void setTotalTime(Duration totalTime) {
        this.totalTime = totalTime == null ? Duration.ZERO : totalTime;
        if (!isRunning()) {
            remaining = this.totalTime;
            elapsedAccumulated = Duration.ZERO;
        }
    }

    /**
     * Returns the amount of time remaining on the countdown. If the timer is
     * currently running the returned value reflects the time left at the moment
     * of the call.
     *
     * @return remaining countdown duration; never negative
     */
    public Duration getRemaining() {
        if (isRunning()) {
            Duration elapsed = Duration.between(runningSince, Instant.now());
            return remaining.minus(elapsed).isNegative() ? Duration.ZERO : remaining.minus(elapsed);
        }
        return remaining;
    }

    /**
     * Starts the timer if it is not already running. Calling this method when
     * the timer is running has no effect.
     */
    public void start() {
        if (!isRunning()) {
            runningSince = Instant.now();
        }
    }

    /**
     * Pauses the timer, preserving the remaining duration based on the elapsed
     * time since it was started. Calling this when the timer is not running has
     * no effect.
     */
    public void pause() {
        if (isRunning()) {
            Duration elapsedSinceStart = Duration.between(runningSince, Instant.now());
            elapsedAccumulated = elapsedAccumulated.plus(elapsedSinceStart);
            remaining = getRemaining();
            runningSince = null;
        }
    }

    /**
     * Resets the timer so that the remaining time is equal to the total time
     * and clears any accumulated elapsed duration. The timer is left in a
     * non-running state.
     */
    public void reset() {
        runningSince = null;
        remaining = totalTime;
        elapsedAccumulated = Duration.ZERO;
    }

    /**
     * Indicates whether the timer is currently running.
     *
     * @return {@code true} if the timer has been started and not paused
     */
    public boolean isRunning() {
        return runningSince != null;
    }

    /**
     * Provides the {@link Instant} at which the timer last started running, if
     * it is currently active.
     *
     * @return optional start instant, empty when not running
     */
    public Optional<Instant> getRunningSince() {
        return Optional.ofNullable(runningSince);
    }

    /**
     * Explicitly sets the amount of time remaining. When the timer has a
     * non-zero total duration this method also adjusts the internally tracked
     * elapsed duration so that elapsed + remaining equals the total time.
     *
     * @param remaining the remaining countdown duration; {@code null} treated as zero
     */
    public void setRemaining(Duration remaining) {
        this.remaining = remaining == null ? Duration.ZERO : remaining;
        if (!totalTime.isZero()) {
            Duration derivedElapsed = totalTime.minus(this.remaining);
            if (!derivedElapsed.isNegative()) {
                elapsedAccumulated = derivedElapsed;
            }
        }
    }

    /**
     * Returns the amount of time that has elapsed since the timer started. If
     * the timer is running the calculation includes the time since the last
     * start; otherwise it returns the accumulated elapsed duration.
     *
     * @return elapsed duration; never negative
     */
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
