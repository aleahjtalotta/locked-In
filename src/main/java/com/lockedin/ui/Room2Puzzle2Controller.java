package com.lockedin.ui;

/**
 * Second puzzle controller for Room 2.
 */
public class Room2Puzzle2Controller extends BasePuzzleController {
    public Room2Puzzle2Controller() {
        super(GameState::completeRoom2Puzzle2, GameState::getNextHubScreen, 304L);
    }
}
