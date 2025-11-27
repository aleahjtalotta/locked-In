package com.lockedin.ui;

/**
 * Maps solved puzzle counts to the appropriate FXML destination.
 */
public final class ProgressNavigator {
    private ProgressNavigator() {
    }

    public static String destinationForSolvedCount(int solvedCount) {
        if (solvedCount >= 3) {
            return "/com/ourgroup1/GameCompleteExit.fxml";
        }
        if (solvedCount >= 2) {
            return "/com/ourgroup1/ChooseDoorRoom1and2Complete.fxml";
        }
        if (solvedCount >= 1) {
            return "/com/ourgroup1/ChooseDoorRoom1Complete.fxml";
        }
        return "/com/ourgroup1/ChooseDoorScreen.fxml";
    }
}
