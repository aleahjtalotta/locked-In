package com.lockedin.ui;

/**
 * First puzzle controller for Room 2.
 */
public class Room2Puzzle1Controller extends BasePuzzleController {
    public Room2Puzzle1Controller() {
        super(GameState::completeRoom2Puzzle1, () -> "Room2Puzzle2.fxml", 303L);
    }
}
