package com.classes;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class DataWriterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void saveGameWritesRoomsAndUsersFilesWithExpectedContent() throws Exception {
        GameSystemFixture fixture = createPopulatedGameSystem();
        Path outputDir = temporaryFolder.newFolder("save-success").toPath();

        DataWriter writer = new DataWriter(outputDir);
        assertTrue(writer.saveGame(fixture.gameSystem));

        Path roomsPath = outputDir.resolve("rooms.json");
        Path usersPath = outputDir.resolve("users.json");
        assertTrue("rooms.json should exist after a successful save", Files.exists(roomsPath));
        assertTrue("users.json should exist after a successful save", Files.exists(usersPath));

        JSONParser parser = new JSONParser();
        JSONObject roomsJson = (JSONObject) parser.parse(
                Files.newBufferedReader(roomsPath, StandardCharsets.UTF_8));
        assertEquals(fixture.gameSystemLegacyId.longValue(), roomsJson.get("gameSystemID"));
        assertEquals(fixture.gameSystem.getDifficulty().name(), roomsJson.get("currentDifficulty"));

        JSONObject timerJson = (JSONObject) roomsJson.get("timer");
        assertNotNull("Timer information should be written", timerJson);
        assertEquals(0L, timerJson.get("timerID"));
        assertEquals("00:45:00", timerJson.get("totalTime"));
        assertEquals("00:12:00", timerJson.get("timeRemaining"));

        JSONArray hintsArray = (JSONArray) roomsJson.get("hints");
        assertEquals(2, hintsArray.size());
        JSONObject firstHintJson = (JSONObject) hintsArray.get(0);
        assertEquals(fixture.legacyHint.getLegacyId(), firstHintJson.get("hintID"));
        assertEquals(fixture.legacyHint.getText(), firstHintJson.get("hintText"));

        JSONObject secondHintJson = (JSONObject) hintsArray.get(1);
        assertEquals(fallbackFromUuid(fixture.fallbackHint.getId()), secondHintJson.get("hintID"));
        assertEquals(fixture.fallbackHint.getText(), secondHintJson.get("hintText"));

        JSONObject leaderboardJson = (JSONObject) roomsJson.get("leaderboard");
        JSONArray scoresArray = (JSONArray) leaderboardJson.get("scores");
        assertEquals(1, scoresArray.size());
        JSONObject scoreJson = (JSONObject) scoresArray.get(0);
        assertEquals(fixture.scoreEntry.getLegacyId(), scoreJson.get("scoreEntryID"));
        assertEquals(fixture.scoreEntry.getPlayerName(), scoreJson.get("playerName"));
        assertEquals(fixture.scoreEntry.getScore(), ((Number) scoreJson.get("score")).intValue());
        assertEquals("00:09:00", scoreJson.get("completionTime"));

        JSONArray roomsArray = (JSONArray) roomsJson.get("rooms");
        assertEquals(1, roomsArray.size());
        JSONObject roomJson = (JSONObject) roomsArray.get(0);
        assertEquals(fixture.room.getLegacyId().longValue(), roomJson.get("roomID"));

        JSONArray roomItemsArray = (JSONArray) roomJson.get("items");
        assertEquals(1, roomItemsArray.size());
        JSONObject roomItemJson = (JSONObject) roomItemsArray.get(0);
        assertEquals(fixture.roomItem.getLegacyId(), roomItemJson.get("itemID"));
        assertEquals(fixture.roomItem.getName(), roomItemJson.get("itemName"));
        assertEquals(fixture.roomItem.isReusable(), roomItemJson.get("isReusable"));

        JSONArray puzzlesArray = (JSONArray) roomJson.get("puzzles");
        assertEquals(2, puzzlesArray.size());
        JSONObject multipleChoiceJson = (JSONObject) puzzlesArray.get(0);
        assertEquals(fixture.multipleChoicePuzzle.getLegacyId(), multipleChoiceJson.get("puzzleName"));
        assertEquals(fixture.multipleChoicePuzzle.getName(), multipleChoiceJson.get("name"));
        assertEquals(fixture.multipleChoicePuzzle.getDescription(), multipleChoiceJson.get("description"));
        assertEquals(fixture.multipleChoicePuzzle.getReward(), multipleChoiceJson.get("reward"));
        assertEquals(fixture.multipleChoicePuzzle.getType().name(), multipleChoiceJson.get("type"));
        assertEquals(fixture.multipleChoicePuzzle.isSolved(), multipleChoiceJson.get("solved"));
        JSONArray optionsArray = (JSONArray) multipleChoiceJson.get("options");
        assertEquals(fixture.multipleChoicePuzzle.getOptions(), optionsArray);
        assertEquals(fixture.multipleChoicePuzzle.getCorrectOption(), multipleChoiceJson.get("correctOption"));

        JSONObject sequencePuzzleJson = (JSONObject) puzzlesArray.get(1);
        assertEquals(fallbackFromUuid(fixture.sequencePuzzle.getId()), sequencePuzzleJson.get("puzzleName"));
        assertEquals(fixture.sequencePuzzle.getType().name(), sequencePuzzleJson.get("type"));
        assertEquals(fixture.sequencePuzzle.isSolved(), sequencePuzzleJson.get("solved"));
        JSONArray sequenceArray = (JSONArray) sequencePuzzleJson.get("sequence");
        assertEquals(fixture.sequencePuzzle.getExpectedSequence(), sequenceArray);

        JSONArray usersArray = (JSONArray) parser.parse(
                Files.newBufferedReader(usersPath, StandardCharsets.UTF_8));
        assertEquals(1, usersArray.size());
        JSONObject userJson = (JSONObject) usersArray.get(0);
        assertEquals(fixture.player.getLegacyId().longValue(), userJson.get("playerID"));
        assertEquals(fixture.player.getName(), userJson.get("name"));
        assertEquals(fixture.player.getEmail(), userJson.get("email"));
        assertEquals(fixture.player.getAvatar(), userJson.get("avatar"));
        assertEquals(fixture.player.getCurrentScore(), ((Number) userJson.get("currentScore")).intValue());

        JSONArray playerItemsArray = (JSONArray) userJson.get("items");
        assertEquals(1, playerItemsArray.size());
        JSONObject inventoryItemJson = (JSONObject) playerItemsArray.get(0);
        assertEquals(fallbackFromUuid(fixture.inventoryItem.getId()), inventoryItemJson.get("itemID"));
        assertEquals(fixture.inventoryItem.getName(), inventoryItemJson.get("itemName"));
        assertEquals(fixture.inventoryItem.isReusable(), inventoryItemJson.get("isReusable"));

        JSONObject statisticsJson = (JSONObject) userJson.get("statistics");
        assertEquals(fixture.statistics.getGamesPlayed(), ((Number) statisticsJson.get("gamesPlayed")).intValue());
        assertEquals(fixture.statistics.getPuzzlesSolved(), ((Number) statisticsJson.get("puzzlesSolved")).intValue());
        assertEquals("00:40:00", statisticsJson.get("avgTime"));
        assertEquals(fixture.statistics.getGamesWon(), ((Number) statisticsJson.get("gamesWon")).intValue());

        JSONArray solvedArray = (JSONArray) userJson.get("solvedPuzzles");
        assertEquals(1, solvedArray.size());
        assertEquals(fixture.sequencePuzzle.getId().toString(), solvedArray.get(0));
    }

    @Test
    public void saveGameReturnsFalseWhenDestinationPathIsAFile() throws Exception {
        Path notDirectory = temporaryFolder.newFile("existing-file.tmp").toPath();
        GameSystem system = new GameSystem();

        DataWriter writer = new DataWriter(notDirectory);

        assertFalse("Saving to a regular file should fail", writer.saveGame(system));
        assertTrue("The original file should remain untouched", Files.isRegularFile(notDirectory));
        assertFalse("No rooms.json should be created for a failed save",
                Files.exists(notDirectory.resolve("rooms.json")));
    }

    private GameSystemFixture createPopulatedGameSystem() {
        GameSystem system = new GameSystem(UUID.fromString("00000000-0000-0000-0000-000000000111"));
        Long legacyId = 77L;
        system.setLegacyId(legacyId);
        system.setDifficulty(DifficultyLevel.HARD);

        Timer timer = system.getTimer();
        timer.setTotalTime(Duration.ofMinutes(45));
        timer.setRemaining(Duration.ofMinutes(12));

        Hints hints = system.getHints();
        Hint legacyHint = new Hint(UUID.fromString("00000000-0000-0000-0000-000000000222"),
                17L, "Look under the mat");
        hints.addHint(legacyHint);
        Hint fallbackHint = new Hint(UUID.fromString("00000000-0000-0000-0000-000000000333"),
                null, "Check the bookshelf");
        hints.addHint(fallbackHint);

        Leaderboard leaderboard = system.getLeaderboard();
        ScoreEntry scoreEntry = new ScoreEntry(
                UUID.fromString("00000000-0000-0000-0000-000000000444"),
                29L, "Morgan", 980, Duration.ofMinutes(9));
        leaderboard.addScoreEntry(scoreEntry);

        Room room = new Room(UUID.fromString("00000000-0000-0000-0000-000000000555"), 41);
        Item roomItem = new Item(UUID.fromString("00000000-0000-0000-0000-000000000666"),
                53L, "Skeleton Key", true);
        room.addItem(roomItem);
        MultipleChoicePuzzle multipleChoicePuzzle = new MultipleChoicePuzzle(
                UUID.fromString("00000000-0000-0000-0000-000000000777"),
                67L, "Door Choice", "Choose the door that opens",
                "Golden Ticket", List.of("Red", "Green", "Blue"), "Green", false);
        SequencePuzzle sequencePuzzle = new SequencePuzzle(
                UUID.fromString("00000000-0000-0000-0000-000000000888"),
                null, "Dial Sequence", "Rotate the dials in order",
                "Map Fragment", List.of("North", "East", "South"), true);
        room.addPuzzle(multipleChoicePuzzle);
        room.addPuzzle(sequencePuzzle);
        system.getRooms().add(room);

        Item inventoryItem = new Item(UUID.fromString("00000000-0000-0000-0000-000000000999"),
                null, "Decoder Ring", false);
        ItemList inventory = new ItemList();
        inventory.add(inventoryItem);
        Statistics statistics = new Statistics(12, 20, Duration.ofMinutes(40), 7);
        Player player = new Player(
                UUID.fromString("00000000-0000-0000-0000-000000001000"),
                85, "Morgan", "morgan@example.com", "avatar.png",
                inventory, statistics, 1234, Set.of(sequencePuzzle.getId()));
        system.getPlayers().add(player);

        return new GameSystemFixture(system, legacyId, legacyHint, fallbackHint,
                room, roomItem, multipleChoicePuzzle, sequencePuzzle, scoreEntry,
                player, inventoryItem, statistics);
    }

    private long fallbackFromUuid(UUID id) {
        long value = id.getLeastSignificantBits();
        return value < 0 ? -value : value;
    }

    private static final class GameSystemFixture {
        final GameSystem gameSystem;
        final Long gameSystemLegacyId;
        final Hint legacyHint;
        final Hint fallbackHint;
        final Room room;
        final Item roomItem;
        final MultipleChoicePuzzle multipleChoicePuzzle;
        final SequencePuzzle sequencePuzzle;
        final ScoreEntry scoreEntry;
        final Player player;
        final Item inventoryItem;
        final Statistics statistics;

        GameSystemFixture(GameSystem gameSystem,
                          Long gameSystemLegacyId,
                          Hint legacyHint,
                          Hint fallbackHint,
                          Room room,
                          Item roomItem,
                          MultipleChoicePuzzle multipleChoicePuzzle,
                          SequencePuzzle sequencePuzzle,
                          ScoreEntry scoreEntry,
                          Player player,
                          Item inventoryItem,
                          Statistics statistics) {
            this.gameSystem = gameSystem;
            this.gameSystemLegacyId = gameSystemLegacyId;
            this.legacyHint = legacyHint;
            this.fallbackHint = fallbackHint;
            this.room = room;
            this.roomItem = roomItem;
            this.multipleChoicePuzzle = multipleChoicePuzzle;
            this.sequencePuzzle = sequencePuzzle;
            this.scoreEntry = scoreEntry;
            this.player = player;
            this.inventoryItem = inventoryItem;
            this.statistics = statistics;
        }
    }
}
