package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class GameSystemTest {

    @Test
    public void constructorWithExplicitIdInitializesDefaults() {
        UUID id = UUID.randomUUID();

        GameSystem system = new GameSystem(id);

        assertSame(id, system.getId());
        assertNotNull(system.getRooms());
        assertNotNull(system.getPuzzles());
        assertNotNull(system.getPlayers());
        assertNotNull(system.getLeaderboard());
        assertNotNull(system.getHints());
        assertNotNull(system.getTimer());
        assertNotNull(system.getProgress());
        assertEquals(DifficultyLevel.MEDIUM, system.getDifficulty());
    }

    @Test
    public void setRoomsUsesProvidedInstanceAndFallsBackToNewListWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        RoomList customRooms = new RoomList();

        system.setRooms(customRooms);
        assertSame(customRooms, system.getRooms());

        system.setRooms(null);
        assertNotNull(system.getRooms());
        assertNotSame(customRooms, system.getRooms());
    }

    @Test
    public void setPuzzlesUsesProvidedInstanceAndFallsBackToNewListWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        PuzzleList customPuzzles = new PuzzleList();

        system.setPuzzles(customPuzzles);
        assertSame(customPuzzles, system.getPuzzles());

        system.setPuzzles(null);
        assertNotNull(system.getPuzzles());
        assertNotSame(customPuzzles, system.getPuzzles());
    }

    @Test
    public void setPlayersUsesProvidedInstanceAndFallsBackToNewListWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        PlayerList customPlayers = new PlayerList();

        system.setPlayers(customPlayers);
        assertSame(customPlayers, system.getPlayers());

        system.setPlayers(null);
        assertNotNull(system.getPlayers());
        assertNotSame(customPlayers, system.getPlayers());
    }

    @Test
    public void setLeaderboardUsesProvidedInstanceAndFallsBackToNewInstanceWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        Leaderboard customLeaderboard = new Leaderboard();

        system.setLeaderboard(customLeaderboard);
        assertSame(customLeaderboard, system.getLeaderboard());

        system.setLeaderboard(null);
        assertNotNull(system.getLeaderboard());
        assertNotSame(customLeaderboard, system.getLeaderboard());
    }

    @Test
    public void setHintsUsesProvidedInstanceAndFallsBackToNewInstanceWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        Hints customHints = new Hints();

        system.setHints(customHints);
        assertSame(customHints, system.getHints());

        system.setHints(null);
        assertNotNull(system.getHints());
        assertNotSame(customHints, system.getHints());
    }

    @Test
    public void setTimerUsesProvidedInstanceAndFallsBackToNewInstanceWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        Timer customTimer = new Timer();

        system.setTimer(customTimer);
        assertSame(customTimer, system.getTimer());

        system.setTimer(null);
        assertNotNull(system.getTimer());
        assertNotSame(customTimer, system.getTimer());
    }

    @Test
    public void setDifficultyUsesProvidedValueAndFallsBackToMediumWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());

        system.setDifficulty(DifficultyLevel.HARD);
        assertEquals(DifficultyLevel.HARD, system.getDifficulty());

        system.setDifficulty(null);
        assertEquals(DifficultyLevel.MEDIUM, system.getDifficulty());
    }

    @Test
    public void setProgressUsesProvidedInstanceAndFallsBackToNewInstanceWhenNull() {
        GameSystem system = new GameSystem(UUID.randomUUID());
        Progress customProgress = new Progress();

        system.setProgress(customProgress);
        assertSame(customProgress, system.getProgress());

        system.setProgress(null);
        assertNotNull(system.getProgress());
        assertNotSame(customProgress, system.getProgress());
    }
}
