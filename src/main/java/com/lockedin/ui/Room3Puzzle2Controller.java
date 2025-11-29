package com.lockedin.ui;

/**
 * Second puzzle controller for Room 3.
 */
public class Room3Puzzle2Controller extends BasePuzzleController {
    public Room3Puzzle2Controller() {
        super(GameState::completeRoom3Puzzle2, GameState::getNextHubScreen);
    }
}
