package com.classes;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlayerTest {

    private Player createPlayer(String name, String email, int score, Set<UUID> solved) {
        return new Player(
                UUID.randomUUID(),
                10,
                name,
                email,
                "avatar.png",
                null,
                null,
                score,
                solved
        );
    }

    @Test
    public void constructorClampsNegativeInitialScoreToZero() {
        Player player = createPlayer("Ada", "ada@example.com", -15, Set.of());

        assertEquals(0, player.getCurrentScore());
    }

    @Test
    public void constructorDefaultsNullNameToFallback() {
        Player player = createPlayer(null, "lex@example.com", 0, Set.of());

        assertEquals("Unknown Player", player.getName());
    }

    @Test
    public void constructorDefaultsNullEmailToEmptyString() {
        Player player = createPlayer("Lex", null, 0, Set.of());

        assertEquals("", player.getEmail());
    }

    @Test
    public void addScoreIncreasesScoreByDelta() {
        Player player = createPlayer("Ada", "ada@example.com", 10, Set.of());

        player.addScore(7);

        assertEquals(17, player.getCurrentScore());
    }

    @Test
    public void addScoreDoesNotAllowNegativeTotals() {
        Player player = createPlayer("Ada", "ada@example.com", 5, Set.of());

        player.addScore(-20);

        assertEquals(0, player.getCurrentScore());
    }

    @Test
    public void setNameIgnoresNullValues() {
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of());

        player.setName(null);

        assertEquals("Ada", player.getName());
    }

    @Test
    public void setEmailIgnoresNullValues() {
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of());

        player.setEmail(null);

        assertEquals("ada@example.com", player.getEmail());
    }

    @Test
    public void setSolvedPuzzleIdsReplacesExistingValues() {
        UUID originalId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of(originalId));

        player.setSolvedPuzzleIds(List.of(newId));

        assertTrue(player.getSolvedPuzzleIds().contains(newId));
        assertFalse(player.getSolvedPuzzleIds().contains(originalId));
    }

    @Test
    public void setSolvedPuzzleIdsClearsEntriesWhenNullProvided() {
        UUID solvedId = UUID.randomUUID();
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of(solvedId));

        player.setSolvedPuzzleIds(null);

        assertTrue(player.getSolvedPuzzleIds().isEmpty());
    }

    @Test
    public void markPuzzleSolvedAddsIdentifierWhenNonNull() {
        UUID solvedId = UUID.randomUUID();
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of());

        player.markPuzzleSolved(solvedId);

        assertTrue(player.getSolvedPuzzleIds().contains(solvedId));
    }

    @Test
    public void markPuzzleSolvedIgnoresNullIdentifier() {
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of());

        player.markPuzzleSolved(null);

        assertTrue(player.getSolvedPuzzleIds().isEmpty());
    }

    @Test
    public void clearSolvedPuzzlesRemovesAllEntries() {
        Player player = createPlayer("Ada", "ada@example.com", 0, Set.of(UUID.randomUUID()));

        player.clearSolvedPuzzles();

        assertTrue(player.getSolvedPuzzleIds().isEmpty());
    }

    @Test
    public void constructorCreatesInventoryAndStatisticsWhenNullProvided() {
        Player player = new Player(
                UUID.randomUUID(),
                5,
                "Ada",
                "ada@example.com",
                "avatar.png",
                null,
                null,
                0,
                null
        );

        assertNotNull(player.getInventory());
        assertNotNull(player.getStatistics());
    }
}
