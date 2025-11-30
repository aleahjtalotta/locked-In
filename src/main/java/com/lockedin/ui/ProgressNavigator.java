package com.lockedin.ui;

/**
 * Maps solved puzzle counts to the appropriate FXML destination.
 */
public final class ProgressNavigator {
    private ProgressNavigator() {
    }

    public static String destinationForSolvedCount(int solvedCount) {
        // Derive completion from the active player's solved puzzles so the result is
        // always per-user and not influenced by previous sessions.
        boolean r1 = isRoomComplete(301L, 302L);
        boolean r2 = isRoomComplete(303L, 304L);
        boolean r3 = isRoomComplete(305L, 306L);

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

    private static boolean isRoomComplete(long firstPuzzleLegacyId, long secondPuzzleLegacyId) {
        return isPuzzleSolved(firstPuzzleLegacyId) && isPuzzleSolved(secondPuzzleLegacyId);
    }

    private static boolean isPuzzleSolved(long puzzleLegacyId) {
        return SessionContext.getActivePlayer()
                .flatMap(player -> PuzzleProvider.findPuzzleByLegacyId(puzzleLegacyId)
                        .map(p -> player.getSolvedPuzzleIds().contains(p.getId())))
                .orElse(false);
    }
}
