package com.lockedin.ui;

import com.classes.Player;
import java.util.Optional;

/**
 * Stores the active player for the current UI session so screens can make
 * routing decisions without reloading user data.
 */
public final class SessionContext {
    private static Player activePlayer;

    private SessionContext() {
    }

    public static void setActivePlayer(Player player) {
        activePlayer = player;
    }

    public static Optional<Player> getActivePlayer() {
        return Optional.ofNullable(activePlayer);
    }

    public static int getSolvedPuzzleCount() {
        return activePlayer == null ? 0 : activePlayer.getSolvedPuzzleIds().size();
    }

    public static void clear() {
        activePlayer = null;
    }
}
