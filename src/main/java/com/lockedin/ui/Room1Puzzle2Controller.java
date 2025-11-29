package com.lockedin.ui;

/**
 * Second puzzle controller for Room 1.
 */
public class Room1Puzzle2Controller extends BasePuzzleController {
    public Room1Puzzle2Controller() {
        super(GameState::completeRoom1Puzzle2, GameState::getNextHubScreen);
    }
}
