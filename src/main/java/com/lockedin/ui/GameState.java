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
        if (!room1Complete && !room2Complete && !room3Complete) {
            return "ChooseDoorScreen.fxml";
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
        if (room1Complete && room2Complete && !room3Complete) {
            return "ChooseDoorRoom1and2Complete.fxml";
        }
        if (room1Complete && room3Complete && !room2Complete) {
            return "ChooseDoorRoom1and3Complete.fxml";
        }
        if (room2Complete && room3Complete && !room1Complete) {
            return "ChooseDoorRoom2and3Complete.fxml";
        }
        if (room1Complete && room2Complete && room3Complete) {
            return "GameCompleteExit.fxml";
        }
        return "ChooseDoorScreen.fxml";
    }
}
