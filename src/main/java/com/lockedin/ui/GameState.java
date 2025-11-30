package com.lockedin.ui;

/**
 * Tracks in-progress and completed puzzles/rooms for the current play session.
 */
public final class GameState {

    private GameState() {
    }

    public static boolean room1Puzzle1Done;
    public static boolean room1Puzzle2Done;
    public static boolean room2Puzzle1Done;
    public static boolean room2Puzzle2Done;
    public static boolean room3Puzzle1Done;
    public static boolean room3Puzzle2Done;

    public static boolean room1Complete;
    public static boolean room2Complete;
    public static boolean room3Complete;

    public static void reset() {
        room1Puzzle1Done = false;
        room1Puzzle2Done = false;
        room2Puzzle1Done = false;
        room2Puzzle2Done = false;
        room3Puzzle1Done = false;
        room3Puzzle2Done = false;
        room1Complete = false;
        room2Complete = false;
        room3Complete = false;
    }

    public static void completeRoom1Puzzle1() {
        room1Puzzle1Done = true;
    }

    public static void completeRoom1Puzzle2() {
        room1Puzzle2Done = true;
        room1Complete = true;
    }

    public static void completeRoom2Puzzle1() {
        room2Puzzle1Done = true;
    }

    public static void completeRoom2Puzzle2() {
        room2Puzzle2Done = true;
        room2Complete = true;
    }

    public static void completeRoom3Puzzle1() {
        room3Puzzle1Done = true;
    }

    public static void completeRoom3Puzzle2() {
        room3Puzzle2Done = true;
        room3Complete = true;
    }

    public static String getNextHubScreen() {
        if (room1Complete && room2Complete && room3Complete) {
            return "GameCompleteExit.fxml";
        }
        if (room1Complete && room2Complete && !room3Complete) {
            return "ChooseDoorRoom1and2Complete.fxml";
        }
        if (room1Complete && room3Complete && !room2Complete) {
            return "ChooseDoorRoom1and3Complete.fxml";
        }
        if (room2Complete && room3Complete && !room1Complete) {
            return "ChooseDoorRoom2and3Complete.fxml";
        }
        if (room1Complete && !room2Complete && !room3Complete) {
            return "ChooseDoorRoom1Complete.fxml";
        }
        if (room2Complete && !room1Complete && !room3Complete) {
            return "ChooseDoorRoom2Complete.fxml";
        }
        if (room3Complete && !room1Complete && !room2Complete) {
            return "ChooseDoorRoom3Complete.fxml";
        }
        return "ChooseDoorScreen.fxml";
    }

    public static void syncFrom(com.classes.GameSystem system, com.classes.Player activePlayer) {
        reset();
        if (system == null || activePlayer == null) {
            return;
        }
        var solved = new java.util.HashSet<java.util.UUID>(activePlayer.getSolvedPuzzleIds());

        applyIfSolved(system, solved, 301L, GameState::completeRoom1Puzzle1);
        applyIfSolved(system, solved, 302L, GameState::completeRoom1Puzzle2);
        applyIfSolved(system, solved, 303L, GameState::completeRoom2Puzzle1);
        applyIfSolved(system, solved, 304L, GameState::completeRoom2Puzzle2);
        applyIfSolved(system, solved, 305L, GameState::completeRoom3Puzzle1);
        applyIfSolved(system, solved, 306L, GameState::completeRoom3Puzzle2);
    }

    private static void applyIfSolved(com.classes.GameSystem system,
                                      java.util.Set<java.util.UUID> solved,
                                      long legacyId,
                                      Runnable action) {
        system.getPuzzles().asList().stream()
                .filter(p -> p.getLegacyId() != null && p.getLegacyId() == legacyId)
                .findFirst()
                .map(com.classes.Puzzle::getId)
                .filter(solved::contains)
                .ifPresent(id -> action.run());
    }
}
