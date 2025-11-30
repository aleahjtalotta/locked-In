package com.lockedin.ui;

/**
 * Maps solved puzzle counts to the appropriate FXML destination.
 */
public final class ProgressNavigator {
    private ProgressNavigator() {
    }

    public static String destinationForSolvedCount(int solvedCount) {
        // Use room completion flags instead of raw solved puzzle count so hub selection
        // matches which rooms are fully finished (both puzzles solved).
        boolean r1 = GameState.room1Complete;
        boolean r2 = GameState.room2Complete;
        boolean r3 = GameState.room3Complete;

        if (r1 && r2 && r3) {
            return "/com/ourgroup1/GameCompleteExit.fxml";
        }
        if (r1 && r2 && !r3) {
            return "/com/ourgroup1/ChooseDoorRoom1and2Complete.fxml";
        }
        if (r2 && r3 && !r1) {
            return "/com/ourgroup1/ChooseDoorRoom2and3Complete.fxml";
        }
        if (r1 && r3 && !r2) {
            return "/com/ourgroup1/ChooseDoorRoom1and3Complete.fxml";
        }
        if (r1 && !r2 && !r3) {
            return "/com/ourgroup1/ChooseDoorRoom1Complete.fxml";
        }
        if (r2 && !r1 && !r3) {
            return "/com/ourgroup1/ChooseDoorRoom2Complete.fxml";
        }
        if (r3 && !r1 && !r2) {
            return "/com/ourgroup1/ChooseDoorRoom3Complete.fxml";
        }
        return "/com/ourgroup1/ChooseDoorScreen.fxml";
    }
}
