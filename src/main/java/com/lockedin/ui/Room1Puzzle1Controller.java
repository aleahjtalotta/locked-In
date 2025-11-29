package com.lockedin.ui;

/**
 * First puzzle controller for Room 1.
 */
public class Room1Puzzle1Controller extends BasePuzzleController {
    public Room1Puzzle1Controller() {
        super(GameState::completeRoom1Puzzle1, () -> "Room1Puzzle2.fxml");
    }
}
