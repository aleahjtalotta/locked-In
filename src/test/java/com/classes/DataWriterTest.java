package com.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Integration-style tests for {@link DataWriter}. The tests exercise the complete
 * JSON documents produced by {@link DataWriter#saveGame(GameSystem)} and validate
 * both the happy path and expected error handling.
 */
public class DataWriterTest {

    private static final UUID GAME_SYSTEM_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID TIMER_HINT_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID GENERATED_HINT_ID = UUID.fromString("00000000-0000-0000-8000-000000000001");
    private static final UUID ROOM_ONE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ROOM_TWO_ID = UUID.fromString("00000000-0000-0000-8000-000000000011");
    private static final UUID ITEM_KEY_ID = UUID.fromString("00000000-0000-0000-0000-000000000700");
    private static final UUID ITEM_CANDLE_ID = UUID.fromString("00000000-0000-0000-8000-000000000701");
    private static final UUID PUZZLE_MC_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID PUZZLE_SEQUENCE_ID = UUID.fromString("00000000-0000-0000-8000-000000000502");
    private static final UUID PUZZLE_RIDDLE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PUZZLE_CODE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PUZZLE_WRITE_IN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SCORE_ALICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID SCORE_BOB_ID = UUID.fromString("00000000-0000-0000-8000-000000000302");
    private static final UUID PLAYER_ALICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000321");
    private static final UUID PLAYER_BOB_ID = UUID.fromString("00000000-0000-0000-8000-000000000002");
    private static final UUID ITEM_MAP_ID = UUID.fromString("00000000-0000-0000-0000-000000000900");

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void saveGame_writesCompleteStateToJsonFiles() throws Exception {
        Path destination = temp.newFolder("game-state").toPath();
        GameSystem system = buildPopulatedGameSystem();

        DataWriter writer = new DataWriter(destination);
        boolean result = writer.saveGame(system);

        assertTrue("saveGame should report success", result);
        assertTrue("rooms.json should exist", Files.exists(destination.resolve("rooms.json")));
        assertTrue("users.json should exist", Files.exists(destination.resolve("users.json")));

        JSONParser parser = new JSONParser();

        JSONObject roomsJson;
        try (Reader reader = Files.newBufferedReader(destination.resolve("rooms.json"), StandardCharsets.UTF_8)) {
            roomsJson = (JSONObject) parser.parse(reader);
        }
        assertRoomsDocument(system, roomsJson);

        JSONArray usersJson;
        try (Reader reader = Files.newBufferedReader(destination.resolve("users.json"), StandardCharsets.UTF_8)) {
            usersJson = (JSONArray) parser.parse(reader);
        }
        assertUsersDocument(system.getPlayers(), usersJson);
    }

    @Test
    public void saveGame_returnsFalseWhenDestinationCannotBeCreated() throws Exception {
        Path conflictingFile = temp.newFile("not-a-directory").toPath();
        DataWriter writer = new DataWriter(conflictingFile);

        boolean result = writer.saveGame(new GameSystem());

        assertFalse("saveGame should return false when an IOException occurs", result);
        assertFalse("rooms.json should not be created on failure",
                Files.exists(conflictingFile.resolve("rooms.json")));
        assertFalse("users.json should not be created on failure",
                Files.exists(conflictingFile.resolve("users.json")));
    }

    private void assertRoomsDocument(GameSystem system, JSONObject roomsJson) {
        assertEquals("game system legacy id should round-trip",
                system.getLegacyId(), roomsJson.get("gameSystemID"));
        assertEquals("difficulty should be serialized by name",
                system.getDifficulty().name(), roomsJson.get("currentDifficulty"));

        JSONObject timerObj = (JSONObject) roomsJson.get("timer");
        assertNotNull("timer object should be present", timerObj);
        assertEquals(Long.valueOf(0L), timerObj.get("timerID"));
        assertEquals("02:30:15", timerObj.get("totalTime"));
        assertEquals("01:30:00", timerObj.get("timeRemaining"));

        JSONArray hintsArray = (JSONArray) roomsJson.get("hints");
        assertEquals("Two hints should be written", 2, hintsArray.size());
        JSONObject legacyHint = (JSONObject) hintsArray.get(0);
        assertEquals(Long.valueOf(201L), legacyHint.get("hintID"));
        assertEquals("Remember the pattern", legacyHint.get("hintText"));
        JSONObject generatedHint = (JSONObject) hintsArray.get(1);
        assertEquals(Long.valueOf(fallbackId(GENERATED_HINT_ID)), generatedHint.get("hintID"));
        assertEquals("Check the corners", generatedHint.get("hintText"));

        JSONObject leaderboardObj = (JSONObject) roomsJson.get("leaderboard");
        JSONArray scoresArray = (JSONArray) leaderboardObj.get("scores");
        assertEquals("Both score entries should serialize", 2, scoresArray.size());
        JSONObject topEntry = (JSONObject) scoresArray.get(0);
        assertEquals(Long.valueOf(301L), topEntry.get("scoreEntryID"));
        assertEquals("Alice", topEntry.get("playerName"));
        assertEquals(Long.valueOf(1500L), topEntry.get("score"));
        assertEquals("01:05:00", topEntry.get("completionTime"));
        JSONObject secondEntry = (JSONObject) scoresArray.get(1);
        assertEquals(Long.valueOf(fallbackId(SCORE_BOB_ID)), secondEntry.get("scoreEntryID"));
        assertEquals("Bob", secondEntry.get("playerName"));
        assertEquals(Long.valueOf(800L), secondEntry.get("score"));
        assertEquals("00:00:00", secondEntry.get("completionTime"));

        JSONArray roomsArray = (JSONArray) roomsJson.get("rooms");
        assertEquals(2, roomsArray.size());
        Map<Long, JSONObject> roomsById = mapRoomsById(roomsArray);

        JSONObject firstRoom = roomsById.get(10L);
        assertNotNull("Room with legacy id 10 should exist", firstRoom);
        assertRoomContents(firstRoom);

        JSONObject secondRoom = roomsById.get(fallbackId(ROOM_TWO_ID));
        assertNotNull("Room with generated id should exist", secondRoom);
        assertEquals(Long.valueOf(fallbackId(ROOM_TWO_ID)), secondRoom.get("roomID"));
        JSONArray secondRoomItems = (JSONArray) secondRoom.get("items");
        assertNotNull(secondRoomItems);
        assertEquals(0, secondRoomItems.size());
        JSONArray secondRoomPuzzles = (JSONArray) secondRoom.get("puzzles");
        assertNotNull(secondRoomPuzzles);
        assertEquals(0, secondRoomPuzzles.size());
    }

    private void assertRoomContents(JSONObject roomJson) {
        JSONArray items = (JSONArray) roomJson.get("items");
        assertEquals(2, items.size());
        JSONObject key = (JSONObject) items.get(0);
        assertEquals(Long.valueOf(700L), key.get("itemID"));
        assertEquals("Master Key", key.get("itemName"));
        assertEquals(Boolean.TRUE, key.get("isReusable"));
        JSONObject candle = (JSONObject) items.get(1);
        assertEquals(Long.valueOf(fallbackId(ITEM_CANDLE_ID)), candle.get("itemID"));
        assertEquals("Candle", candle.get("itemName"));
        assertEquals(Boolean.FALSE, candle.get("isReusable"));

        JSONArray puzzles = (JSONArray) roomJson.get("puzzles");
        assertEquals(5, puzzles.size());

        Map<String, JSONObject> puzzlesByType = new HashMap<>();
        for (Object obj : puzzles) {
            JSONObject puzzle = (JSONObject) obj;
            puzzlesByType.put((String) puzzle.get("type"), puzzle);
        }

        JSONObject mc = puzzlesByType.get("MULTIPLE_CHOICE");
        assertNotNull(mc);
        assertEquals(Long.valueOf(501L), mc.get("puzzleName"));
        assertEquals("Choose the right lever", mc.get("name"));
        assertEquals("Pull the correct lever to open the door.", mc.get("description"));
        assertEquals("A hidden key appears.", mc.get("reward"));
        assertEquals(Boolean.FALSE, mc.get("solved"));
        JSONArray options = (JSONArray) mc.get("options");
        assertEquals(List.of("Left", "Middle", "Right"), options);
        assertEquals("Middle", mc.get("correctOption"));

        JSONObject sequence = puzzlesByType.get("SEQUENCE");
        assertNotNull(sequence);
        assertEquals(Long.valueOf(fallbackId(PUZZLE_SEQUENCE_ID)), sequence.get("puzzleName"));
        assertEquals("SEQUENCE CHALLENGE", sequence.get("name"));
        JSONArray sequenceArray = (JSONArray) sequence.get("sequence");
        assertEquals(List.of("north", "east", "south", "west"), sequenceArray);

        JSONObject riddle = puzzlesByType.get("RIDDLE");
        assertNotNull(riddle);
        assertEquals("I speak without a mouth.", riddle.get("riddle"));
        assertEquals("Echo", riddle.get("answer"));
        assertEquals(Boolean.TRUE, riddle.get("solved"));

        JSONObject codeLock = puzzlesByType.get("CODE_LOCK");
        assertNotNull(codeLock);
        assertEquals(Long.valueOf(fallbackId(PUZZLE_CODE_ID)), codeLock.get("puzzleName"));
        assertEquals("7451", codeLock.get("code"));

        JSONObject writeIn = puzzlesByType.get("WRITE_IN");
        assertNotNull(writeIn);
        assertEquals("open sesame", writeIn.get("correctAnswer"));
    }

    private void assertUsersDocument(PlayerList players, JSONArray usersJson) {
        assertEquals(players.size(), usersJson.size());

        Map<String, JSONObject> usersByName = new HashMap<>();
        for (Object obj : usersJson) {
            JSONObject user = (JSONObject) obj;
            usersByName.put((String) user.get("name"), user);
        }

        JSONObject aliceJson = usersByName.get("Alice");
        assertNotNull("Alice should be serialized", aliceJson);
        assertEquals(Long.valueOf(321L), aliceJson.get("playerID"));
        assertEquals("alice@example.com", aliceJson.get("email"));
        assertEquals("avatar.png", aliceJson.get("avatar"));
        assertEquals(Long.valueOf(1200L), aliceJson.get("currentScore"));
        JSONArray aliceItems = (JSONArray) aliceJson.get("items");
        assertEquals(1, aliceItems.size());
        JSONObject mapItem = (JSONObject) aliceItems.get(0);
        assertEquals(Long.valueOf(900L), mapItem.get("itemID"));
        assertEquals("Map", mapItem.get("itemName"));
        assertEquals(Boolean.FALSE, mapItem.get("isReusable"));

        JSONObject aliceStats = (JSONObject) aliceJson.get("statistics");
        assertEquals(Long.valueOf(3L), aliceStats.get("gamesPlayed"));
        assertEquals(Long.valueOf(7L), aliceStats.get("puzzlesSolved"));
        assertEquals("00:45:00", aliceStats.get("avgTime"));
        assertEquals(Long.valueOf(2L), aliceStats.get("gamesWon"));
        JSONArray aliceSolved = (JSONArray) aliceJson.get("solvedPuzzles");
        assertEquals(2, aliceSolved.size());
        Set<String> solvedIds = new HashSet<>();
        aliceSolved.forEach(id -> solvedIds.add((String) id));
        assertTrue(solvedIds.contains("11111111-1111-1111-1111-111111111111"));
        assertTrue(solvedIds.contains("22222222-2222-2222-2222-222222222222"));

        JSONObject bobJson = usersByName.get("Bob");
        assertNotNull("Bob should be serialized", bobJson);
        assertEquals(Long.valueOf(fallbackId(PLAYER_BOB_ID)), bobJson.get("playerID"));
        assertEquals("bob@example.com", bobJson.get("email"));
        assertEquals("", bobJson.get("avatar"));
        assertEquals(Long.valueOf(0L), bobJson.get("currentScore"));
        JSONArray bobItems = (JSONArray) bobJson.get("items");
        assertEquals(0, bobItems.size());
        JSONObject bobStats = (JSONObject) bobJson.get("statistics");
        assertEquals(Long.valueOf(0L), bobStats.get("gamesPlayed"));
        assertEquals("00:00:00", bobStats.get("avgTime"));
        JSONArray bobSolved = (JSONArray) bobJson.get("solvedPuzzles");
        assertEquals(0, bobSolved.size());
    }

    private GameSystem buildPopulatedGameSystem() {
        GameSystem system = new GameSystem(GAME_SYSTEM_ID);
        system.setLegacyId(999L);
        system.setDifficulty(DifficultyLevel.HARD);

        Timer timer = new Timer();
        timer.setTotalTime(Duration.ofHours(2).plusMinutes(30).plusSeconds(15));
        timer.setRemaining(Duration.ofHours(1).plusMinutes(30));
        system.setTimer(timer);

        Hints hints = new Hints();
        hints.addHint(TIMER_HINT_ID, 201L, "Remember the pattern");
        hints.addHint(GENERATED_HINT_ID, null, "Check the corners");
        system.setHints(hints);

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.addScoreEntry(SCORE_ALICE_ID, 301L,
                "Alice", Duration.ofHours(1).plusMinutes(5), 1500);
        // Negative completion time exercises formatting guard
        leaderboard.addScoreEntry(SCORE_BOB_ID, null,
                "Bob", Duration.ofSeconds(-45), 800);
        system.setLeaderboard(leaderboard);

        Room mainRoom = new Room(ROOM_ONE_ID, 10);
        mainRoom.addItem(new Item(ITEM_KEY_ID, 700L, "Master Key", true));
        mainRoom.addItem(new Item(ITEM_CANDLE_ID, null, "Candle", false));

        List<String> options = List.of("Left", "Middle", "Right");
        MultipleChoicePuzzle mcPuzzle = new MultipleChoicePuzzle(
                PUZZLE_MC_ID,
                501L,
                "Choose the right lever",
                "Pull the correct lever to open the door.",
                "A hidden key appears.",
                options,
                "Middle",
                false
        );
        SequencePuzzle sequencePuzzle = new SequencePuzzle(
                PUZZLE_SEQUENCE_ID,
                null,
                "SEQUENCE CHALLENGE",
                "Follow the compass order.",
                "A secret drawer unlocks.",
                List.of("north", "east", "south", "west"),
                false
        );
        RiddlePuzzle riddlePuzzle = new RiddlePuzzle(
                PUZZLE_RIDDLE_ID,
                502L,
                "Ancient Riddle",
                "Figure me out.",
                "Another key drops.",
                "I speak without a mouth.",
                "Echo",
                true
        );
        CodeLockPuzzle codeLockPuzzle = new CodeLockPuzzle(
                PUZZLE_CODE_ID,
                null,
                "Vault Lock",
                "Enter the four-digit code.",
                "The vault opens.",
                "7451",
                false
        );
        WriteInPuzzle writeInPuzzle = new WriteInPuzzle(
                PUZZLE_WRITE_IN_ID,
                503L,
                "Magic Phrase",
                "Say the words to proceed.",
                "The path ahead clears.",
                "open sesame",
                false
        );

        mainRoom.addPuzzle(mcPuzzle);
        mainRoom.addPuzzle(sequencePuzzle);
        mainRoom.addPuzzle(riddlePuzzle);
        mainRoom.addPuzzle(codeLockPuzzle);
        mainRoom.addPuzzle(writeInPuzzle);

        Room emptyRoom = new Room(ROOM_TWO_ID, null);

        RoomList rooms = new RoomList();
        rooms.add(mainRoom);
        rooms.add(emptyRoom);
        system.setRooms(rooms);

        PuzzleList puzzles = new PuzzleList();
        puzzles.add(mcPuzzle);
        puzzles.add(sequencePuzzle);
        puzzles.add(riddlePuzzle);
        puzzles.add(codeLockPuzzle);
        puzzles.add(writeInPuzzle);
        system.setPuzzles(puzzles);

        PlayerList players = new PlayerList();
        ItemList aliceInventory = new ItemList();
        aliceInventory.add(new Item(ITEM_MAP_ID,
                900L, "Map", false));
        Statistics aliceStats = new Statistics();
        aliceStats.setGamesPlayed(3);
        aliceStats.setPuzzlesSolved(7);
        aliceStats.setAverageCompletionTime(Duration.ofMinutes(45));
        aliceStats.setGamesWon(2);
        Set<UUID> aliceSolved = new HashSet<>();
        aliceSolved.add(riddlePuzzle.getId());
        aliceSolved.add(codeLockPuzzle.getId());
        Player alice = new Player(
                PLAYER_ALICE_ID,
                321,
                "Alice",
                "alice@example.com",
                "avatar.png",
                aliceInventory,
                aliceStats,
                1200,
                aliceSolved
        );
        players.add(alice);

        ItemList bobInventory = new ItemList();
        Statistics bobStats = new Statistics(0, 0, null, 0);
        Player bob = new Player(
                PLAYER_BOB_ID,
                null,
                "Bob",
                "bob@example.com",
                "",
                bobInventory,
                bobStats,
                0,
                new ArrayList<UUID>()
        );
        players.add(bob);

        system.setPlayers(players);

        return system;
    }

    private Map<Long, JSONObject> mapRoomsById(JSONArray roomsArray) {
        Map<Long, JSONObject> rooms = new HashMap<>();
        for (Object obj : roomsArray) {
            JSONObject room = (JSONObject) obj;
            Object id = room.get("roomID");
            if (id instanceof Number number) {
                rooms.put(number.longValue(), room);
            }
        }
        return rooms;
    }

    private long fallbackId(UUID id) {
        long value = id.getLeastSignificantBits();
        return value < 0 ? -value : value;
    }
}
