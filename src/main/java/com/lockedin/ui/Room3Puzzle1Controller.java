package com.lockedin.ui;

/**
 * First puzzle controller for Room 3.
 */
public class Room3Puzzle1Controller extends BasePuzzleController {
    public Room3Puzzle1Controller() {
        super(GameState::completeRoom3Puzzle1, () -> "Room3Puzzle2.fxml", 305L);
    }
}
