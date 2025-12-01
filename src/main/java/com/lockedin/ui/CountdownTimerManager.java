package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.Timer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Coordinates the 15-minute countdown across screens and keeps it in sync with
 * the saved game state.
 */
public final class CountdownTimerManager {
    private static final Path DATA_DIR = Paths.get("JSON");
    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(15);
    private static final Object LOCK = new Object();

    private static Timer timer;
    private static boolean initialized;
    private static Timeline ticker;
    private static final Set<Label> boundLabels = new HashSet<>();

    private CountdownTimerManager() {
    }

    /**
     * Binds a label so it reflects the current countdown in MM:SS.
     *
     * @param label label to update; ignored when {@code null}
     */
    public static void bindLabel(Label label) {
        if (label == null) {
            return;
        }
        synchronized (LOCK) {
            boundLabels.add(label);
        }
        refreshDisplay();
    }

    /**
     * Starts or resumes the countdown if there is time left. The first call
     * initializes the timer to 15 minutes when no duration has been set.
     */
    public static void startIfNeeded() {
        synchronized (LOCK) {
            ensureTimerLoaded();
            if (!initialized) {
                if (timer.getTotalTime().isZero()) {
                    timer.setTotalTime(DEFAULT_DURATION);
                }
                // Always start a fresh 15:00 for a new session entry.
                timer.reset();
                initialized = true;
            }
            if (!timer.isRunning() && !timer.getRemaining().isZero()) {
                timer.start();
            }
            ensureTickerRunning();
            persistTimer();
        }
        refreshDisplay();
    }

    /**
     * Pauses the countdown and writes the remaining time to disk.
     */
    public static void pauseAndPersist() {
        synchronized (LOCK) {
            if (timer == null) {
                return;
            }
            if (timer.isRunning()) {
                timer.pause();
            }
            stopTickerIfIdle();
            persistTimer();
        }
        refreshDisplay();
    }

    /**
     * Pauses the countdown (if running) and saves the current state. Intended
     * for game-end flows so the last time value is preserved.
     */
    public static void finalizeTimer() {
        pauseAndPersist();
    }

    /**
     * Forces bound labels to update to the latest remaining time.
     */
    public static void refreshDisplay() {
        Duration remaining;
        synchronized (LOCK) {
            if (timer == null) {
                remaining = DEFAULT_DURATION;
            } else {
                remaining = timer.getRemaining();
                if (!initialized && remaining.isZero()) {
                    remaining = timer.getTotalTime().isZero() ? DEFAULT_DURATION : timer.getTotalTime();
                }
            }
        }
        String text = format(remaining);
        Platform.runLater(() -> {
            synchronized (LOCK) {
                boundLabels.removeIf(label -> label.getScene() == null);
                for (Label label : boundLabels) {
                    label.setText(text);
                }
            }
        });
    }

    private static void ensureTimerLoaded() {
        if (timer != null) {
            return;
        }
        DataLoader loader = new DataLoader(DATA_DIR);
        Optional<GameSystem> systemOpt = loader.loadGame();
        timer = systemOpt.map(GameSystem::getTimer).orElseGet(Timer::new);
    }

    private static void ensureTickerRunning() {
        if (ticker == null) {
            ticker = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> handleTick()));
            ticker.setCycleCount(Animation.INDEFINITE);
        }
        if (timer != null && timer.isRunning() && ticker.getStatus() != Animation.Status.RUNNING) {
            ticker.play();
        }
    }

    private static void handleTick() {
        boolean shouldPersist = false;
        synchronized (LOCK) {
            if (timer == null) {
                return;
            }
            if (timer.isRunning() && timer.getRemaining().isZero()) {
                timer.pause();
                shouldPersist = true;
            }
            stopTickerIfIdle();
        }
        if (shouldPersist) {
            persistTimer();
        }
        refreshDisplay();
    }

    private static void stopTickerIfIdle() {
        if (ticker != null && (timer == null || !timer.isRunning())) {
            ticker.stop();
        }
    }

    private static void persistTimer() {
        DataLoader loader = new DataLoader(DATA_DIR);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            return;
        }
        GameSystem system = systemOpt.get();
        Timer target = system.getTimer();
        if (target == null) {
            target = new Timer();
            system.setTimer(target);
        }
        synchronized (LOCK) {
            target.setTotalTime(timer.getTotalTime());
            target.setRemaining(timer.getRemaining());
        }
        new DataWriter(DATA_DIR).saveGame(system);
    }

    private static String format(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
