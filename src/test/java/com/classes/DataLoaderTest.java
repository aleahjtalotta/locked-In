package com.classes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

public class DataLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void loadGameParsesRoomsAndUsersFromJson() throws Exception {
        Path directory = temporaryFolder.newFolder("data").toPath();
        Files.writeString(directory.resolve("rooms.json"), roomsJson(), StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("users.json"), usersJson(), StandardCharsets.UTF_8);

        DataLoader loader = new DataLoader(directory);
        Optional<GameSystem> loaded = loader.loadGame();

        assertTrue(loaded.isPresent());
        GameSystem system = loaded.get();

        assertEquals(DifficultyLevel.HARD, system.getDifficulty());
        Timer timer = system.getTimer();
        assertEquals(Duration.ofHours(1).plusMinutes(5), timer.getTotalTime());
        assertEquals(Duration.ofMinutes(15), timer.getRemaining());

        assertEquals(1, system.getHints().getRemainingHints().size());
        assertEquals("Look up", system.getHints().getRemainingHints().get(0).getText());

        assertEquals(1, system.getLeaderboard().getScores().size());
        ScoreEntry scoreEntry = system.getLeaderboard().getScores().get(0);
        assertEquals("Taylor", scoreEntry.getPlayerName());
        assertEquals(25, scoreEntry.getScore());

        assertEquals(1, system.getRooms().size());
        Room room = system.getRooms().asList().get(0);
        assertEquals(1, room.getItems().size());
        assertEquals("Rusty Key", room.getItems().get(0).getName());
        assertEquals(1, room.getPuzzles().size());

        Puzzle puzzle = system.getPuzzles().asList().get(0);
        assertTrue(puzzle instanceof SequencePuzzle);
        assertEquals("Sequence Puzzle", puzzle.getName());
        assertEquals(2, ((SequencePuzzle) puzzle).getExpectedSequence().size());

        assertEquals(1, system.getPlayers().size());
        Player player = system.getPlayers().asList().get(0);
        assertEquals("Robin", player.getName());
        assertEquals("robin@example.com", player.getEmail());
        assertEquals(9, player.getCurrentScore());
        assertTrue(player.getSolvedPuzzleIds().contains(puzzle.getId()));
        assertEquals(2, player.getStatistics().getGamesWon());
    }

    @Test
    public void loadGameReturnsEmptyWhenRoomsJsonIsCorrupt() throws Exception {
        Path directory = temporaryFolder.newFolder("corrupt").toPath();
        Files.write(directory.resolve("rooms.json"), "{not valid json}".getBytes(StandardCharsets.UTF_8));
        Files.writeString(directory.resolve("users.json"), "[]", StandardCharsets.UTF_8);

        DataLoader loader = new DataLoader(directory);
        Optional<GameSystem> loaded = loader.loadGame();

        assertFalse(loaded.isPresent());
    }

    @Test
    public void loadGamePreservesUuidPuzzleIdentifiers() throws Exception {
        Path directory = temporaryFolder.newFolder("uuid-puzzles").toPath();
        String puzzleId = "123e4567-e89b-12d3-a456-426614174000";
        Files.writeString(directory.resolve("rooms.json"), roomsJsonWithPuzzleUuid(puzzleId), StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("users.json"), usersJsonWithSolvedUuid(puzzleId), StandardCharsets.UTF_8);

        GameSystem system = new DataLoader(directory).loadGame().orElseThrow();

        UUID expectedId = UUID.fromString(puzzleId);
        Puzzle loadedPuzzle = system.getPuzzles().asList().get(0);

        assertEquals("Puzzle should retain the UUID stored in JSON", expectedId, loadedPuzzle.getId());
        assertTrue("Room should expose puzzle via the same UUID",
                system.getRooms().asList().get(0).findPuzzle(expectedId).isPresent());
        assertTrue("Player's solved set should still contain the original UUID",
                system.getPlayers().asList().get(0).getSolvedPuzzleIds().contains(expectedId));
    }

    @Test
    public void loadGamePreservesPlayerUuidIdentifiers() throws Exception {
        Path directory = temporaryFolder.newFolder("uuid-players").toPath();
        String playerId = "98c8b4a0-3c3c-4db7-9f42-6e459412abcd";
        Files.writeString(directory.resolve("rooms.json"), minimalRoomsJson(), StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("users.json"), usersJsonWithPlayerUuid(playerId), StandardCharsets.UTF_8);

        GameSystem system = new DataLoader(directory).loadGame().orElseThrow();

        assertEquals("Player UUID from JSON should be retained",
                UUID.fromString(playerId), system.getPlayers().asList().get(0).getId());
    }

    @Test
    public void loadGamePreservesRoomUuidIdentifiers() throws Exception {
        Path directory = temporaryFolder.newFolder("uuid-rooms").toPath();
        String roomId = "321e4567-e89b-12d3-a456-426614174999";
        Files.writeString(directory.resolve("rooms.json"), roomsJsonWithRoomUuid(roomId), StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("users.json"), "[]", StandardCharsets.UTF_8);

        GameSystem system = new DataLoader(directory).loadGame().orElseThrow();

        assertEquals("Room UUID from JSON should be retained",
                UUID.fromString(roomId), system.getRooms().asList().get(0).getId());
    }

    @Test
    public void loadGamePreservesHintUuidIdentifiers() throws Exception {
        Path directory = temporaryFolder.newFolder("uuid-hints").toPath();
        String hintId = "5b9819c6-78f1-4e8d-93f8-4f1e6a5ee111";
        Files.writeString(directory.resolve("rooms.json"), roomsJsonWithHintUuid(hintId), StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("users.json"), "[]", StandardCharsets.UTF_8);

        GameSystem system = new DataLoader(directory).loadGame().orElseThrow();

        Hint loadedHint = system.getHints().getRemainingHints().get(0);
        assertEquals("Hint UUID from JSON should be retained", UUID.fromString(hintId), loadedHint.getId());
    }

    @Test
    public void loadGameRespectsZeroTimerRemaining() throws Exception {
        Path directory = temporaryFolder.newFolder("timer-zero").toPath();
        Files.writeString(directory.resolve("rooms.json"), roomsJsonWithZeroRemainingTimer(), StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("users.json"), "[]", StandardCharsets.UTF_8);

        Timer timer = new DataLoader(directory).loadGame().orElseThrow().getTimer();

        assertEquals("Total time should be parsed from JSON", Duration.ofMinutes(10), timer.getTotalTime());
        assertEquals("Remaining time of zero should be preserved", Duration.ZERO, timer.getRemaining());
    }

    private String roomsJson() {
        return "{\n" +
                "  \"gameSystemID\": 200,\n" +
                "  \"currentDifficulty\": \"HARD\",\n" +
                "  \"timer\": {\n" +
                "    \"totalTime\": \"01:05:00\",\n" +
                "    \"timeRemaining\": \"00:15:00\"\n" +
                "  },\n" +
                "  \"hints\": [\n" +
                "    {\n" +
                "      \"hintID\": 10,\n" +
                "      \"hintText\": \"Look up\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"leaderboard\": {\n" +
                "    \"scores\": [\n" +
                "      {\n" +
                "        \"scoreEntryID\": 3,\n" +
                "        \"playerName\": \"Taylor\",\n" +
                "        \"score\": 25,\n" +
                "        \"completionTime\": \"00:12:00\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"rooms\": [\n" +
                "    {\n" +
                "      \"roomID\": 8,\n" +
                "      \"items\": [\n" +
                "        {\n" +
                "          \"itemID\": 2,\n" +
                "          \"itemName\": \"Rusty Key\",\n" +
                "          \"isReusable\": true\n" +
                "        }\n" +
                "      ],\n" +
                "      \"puzzles\": [\n" +
                "        {\n" +
                "          \"puzzleName\": 7,\n" +
                "          \"name\": \"Sequence Puzzle\",\n" +
                "          \"description\": \"Arrange the steps\",\n" +
                "          \"reward\": \"Door opens\",\n" +
                "          \"type\": \"SEQUENCE\",\n" +
                "          \"sequence\": [\n" +
                "            \"Turn\",\n" +
                "            \"Push\"\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    private String usersJson() {
        return "[\n" +
                "  {\n" +
                "    \"playerID\": 12,\n" +
                "    \"name\": \"Robin\",\n" +
                "    \"email\": \"robin@example.com\",\n" +
                "    \"currentScore\": 9,\n" +
                "    \"statistics\": {\n" +
                "      \"gamesPlayed\": 3,\n" +
                "      \"puzzlesSolved\": 5,\n" +
                "      \"avgTime\": \"00:10:00\",\n" +
                "      \"gamesWon\": 2\n" +
                "    },\n" +
                "    \"solvedPuzzles\": [7]\n" +
                "  }\n" +
                "]\n";
    }

    private String roomsJsonWithPuzzleUuid(String puzzleUuid) {
        return "{\n" +
                "  \"currentDifficulty\": \"MEDIUM\",\n" +
                "  \"rooms\": [\n" +
                "    {\n" +
                "      \"roomID\": 5,\n" +
                "      \"puzzles\": [\n" +
                "        {\n" +
                "          \"puzzleName\": \"" + puzzleUuid + "\",\n" +
                "          \"name\": \"Persisted Puzzle\",\n" +
                "          \"description\": \"\",\n" +
                "          \"reward\": \"\",\n" +
                "          \"type\": \"WRITE_IN\",\n" +
                "          \"correctAnswer\": \"answer\",\n" +
                "          \"solved\": false\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    private String roomsJsonWithRoomUuid(String roomUuid) {
        return "{\n" +
                "  \"rooms\": [\n" +
                "    {\n" +
                "      \"roomID\": \"" + roomUuid + "\",\n" +
                "      \"items\": [],\n" +
                "      \"puzzles\": []\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    private String roomsJsonWithHintUuid(String hintUuid) {
        return "{\n" +
                "  \"hints\": [\n" +
                "    {\n" +
                "      \"hintID\": \"" + hintUuid + "\",\n" +
                "      \"hintText\": \"Check under the mat\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    private String roomsJsonWithZeroRemainingTimer() {
        return "{\n" +
                "  \"timer\": {\n" +
                "    \"totalTime\": \"00:10:00\",\n" +
                "    \"timeRemaining\": \"00:00:00\"\n" +
                "  },\n" +
                "  \"rooms\": []\n" +
                "}\n";
    }

    private String minimalRoomsJson() {
        return "{ \"rooms\": [] }\n";
    }

    private String usersJsonWithSolvedUuid(String puzzleUuid) {
        return "[\n" +
                "  {\n" +
                "    \"playerID\": 99,\n" +
                "    \"name\": \"Jordan\",\n" +
                "    \"email\": \"jordan@example.com\",\n" +
                "    \"currentScore\": 0,\n" +
                "    \"statistics\": {\n" +
                "      \"gamesPlayed\": 0,\n" +
                "      \"puzzlesSolved\": 0,\n" +
                "      \"avgTime\": \"00:00:00\",\n" +
                "      \"gamesWon\": 0\n" +
                "    },\n" +
                "    \"solvedPuzzles\": [\"" + puzzleUuid + "\"]\n" +
                "  }\n" +
                "]\n";
    }

    private String usersJsonWithPlayerUuid(String playerUuid) {
        return "[\n" +
                "  {\n" +
                "    \"playerID\": \"" + playerUuid + "\",\n" +
                "    \"name\": \"Jordan\",\n" +
                "    \"email\": \"jordan@example.com\",\n" +
                "    \"currentScore\": 0,\n" +
                "    \"statistics\": {\n" +
                "      \"gamesPlayed\": 0,\n" +
                "      \"puzzlesSolved\": 0,\n" +
                "      \"avgTime\": \"00:00:00\",\n" +
                "      \"gamesWon\": 0\n" +
                "    },\n" +
                "    \"solvedPuzzles\": []\n" +
                "  }\n" +
                "]\n";
    }
}
