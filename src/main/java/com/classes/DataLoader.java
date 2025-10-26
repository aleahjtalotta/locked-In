package com.classes;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Loads the escape room data from the JSON files.
 * I wrote these notes to remind future students what each piece does.
 */
public class DataLoader {
    private static final String ROOMS_FILE = "rooms.json";
    private static final String USERS_FILE = "users.json";

    private final Path sourceDirectory;
    private final JSONParser parser = new JSONParser();

    /**
     * Builds a loader that looks inside the given folder for JSON files.
     *
     * @param sourceDirectory folder that should contain rooms.json and users.json
     */
    public DataLoader(Path sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * Tries to read the JSON files and return a full game system.
     * If anything goes wrong we just return an empty Optional.
     *
     * @return game system from disk when everything worked, otherwise empty
     */
    public Optional<GameSystem> loadGame() {
        try {
            JSONObject roomsData = readObject(sourceDirectory.resolve(ROOMS_FILE));
            GameSystem system = parseGameSystem(roomsData);

            JSONArray usersData = readArray(sourceDirectory.resolve(USERS_FILE));
            system.setPlayers(parsePlayers(usersData));

            return Optional.of(system);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Reads one JSON object file, or gives back an empty object if the file is missing.
     */
    private JSONObject readObject(Path file) throws IOException, ParseException {
        if (!Files.exists(file)) {
            return new JSONObject();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object result = parser.parse(reader);
            return result instanceof JSONObject ? (JSONObject) result : new JSONObject();
        }
    }

    /**
     * Reads one JSON array file, or gives back an empty array when the file is missing.
     */
    private JSONArray readArray(Path file) throws IOException, ParseException {
        if (!Files.exists(file)) {
            return new JSONArray();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object result = parser.parse(reader);
            if (result instanceof JSONArray array) {
                return array;
            }
            return new JSONArray();
        }
    }

    /**
     * Turns the top-level rooms JSON into our GameSystem object.
     */
    private GameSystem parseGameSystem(JSONObject root) {
        Long legacyId = asNullableLong(root.get("gameSystemID"));
        UUID gameId = deriveUuid("game", legacyId);
        GameSystem system = new GameSystem(gameId);
        system.setLegacyId(legacyId);

        system.setDifficulty(DifficultyLevel.fromString((String) root.get("currentDifficulty")));

        system.setTimer(parseTimer((JSONObject) root.get("timer")));
        system.setHints(parseHints((JSONArray) root.get("hints")));
        system.setLeaderboard(parseLeaderboard((JSONObject) root.get("leaderboard")));

        RoomList rooms = new RoomList();
        PuzzleList puzzles = new PuzzleList();
        JSONArray roomsArray = (JSONArray) root.get("rooms");
        if (roomsArray != null) {
            for (Object entry : roomsArray) {
                if (entry instanceof JSONObject roomObj) {
                    Room room = parseRoom(roomObj, puzzles);
                    rooms.add(room);
                }
            }
        }
        system.setRooms(rooms);
        system.setPuzzles(puzzles);

        return system;
    }

    /**
     * Builds the timer using the saved information.
     */
    private Timer parseTimer(JSONObject timerObj) {
        Timer timer = new Timer();
        if (timerObj != null) {
            timer.setTotalTime(parseDuration((String) timerObj.get("totalTime")));
            Duration remaining = parseDuration((String) timerObj.get("timeRemaining"));
            if (!remaining.isZero()) {
                timer.setRemaining(remaining);
            }
        }
        return timer;
    }

    /**
     * Converts the hints section from JSON into our queue.
     *
     * @param hintsArray raw hints array loaded from {@code rooms.json}; may be 
     * @return populated instance
     */
    private Hints parseHints(JSONArray hintsArray) {
        Hints hints = new Hints();
        if (hintsArray != null) {
            for (Object obj : hintsArray) {
                if (obj instanceof JSONObject hintObj) {
                    Long legacyId = asNullableLong(hintObj.get("hintID"));
                    UUID id = deriveUuid("hint", legacyId);
                    String text = (String) hintObj.getOrDefault("hintText", "");
                    hints.addHint(id, legacyId, text);
                }
            }
        }
        return hints;
    }

    /**
     * Puts the leaderboard JSON into our simple leaderboard class.
     */
    private Leaderboard parseLeaderboard(JSONObject leaderboardObj) {
        Leaderboard leaderboard = new Leaderboard();
        if (leaderboardObj != null) {
            JSONArray scores = (JSONArray) leaderboardObj.get("scores");
            if (scores != null) {
                for (Object entry : scores) {
                    if (entry instanceof JSONObject scoreObj) {
                        Long legacyId = asNullableLong(scoreObj.get("scoreEntryID"));
                        UUID id = deriveUuid("score", legacyId);
                        String playerName = (String) scoreObj.getOrDefault("playerName", "Unknown");
                        int score = asNumber(scoreObj.get("score")).intValue();
                        Duration completionTime = parseDuration((String) scoreObj.get("completionTime"));
                        leaderboard.addScoreEntry(id, legacyId, playerName, completionTime, score);
                    }
                }
            }
        }
        return leaderboard;
    }

    /**
     * Reads one room and also adds its puzzles to the shared puzzle list.
     */
    private Room parseRoom(JSONObject roomObj, PuzzleList puzzleList) {
        Long legacyId = asNullableLong(roomObj.get("roomID"));
        Room room = new Room(deriveUuid("room", legacyId), legacyId == null ? null : legacyId.intValue());

        JSONArray itemsArray = (JSONArray) roomObj.get("items");
        if (itemsArray != null) {
            for (Object item : itemsArray) {
                if (item instanceof JSONObject itemObj) {
                    room.addItem(parseItem(itemObj));
                }
            }
        }

        JSONArray puzzlesArray = (JSONArray) roomObj.get("puzzles");
        if (puzzlesArray != null) {
            for (Object puzzleEntry : puzzlesArray) {
                if (puzzleEntry instanceof JSONObject puzzleObj) {
                    Puzzle puzzle = parsePuzzle(puzzleObj);
                    room.addPuzzle(puzzle);
                    puzzleList.add(puzzle);
                }
            }
        }
        return room;
    }

    /**
     * Converts a JSON item object into the Item class.
     */
    private Item parseItem(JSONObject itemObj) {
        Long legacyId = asNullableLong(itemObj.get("itemID"));
        UUID id = deriveUuid("item", legacyId);
        String name = (String) itemObj.getOrDefault("itemName", "Item");
        boolean reusable = Boolean.TRUE.equals(itemObj.get("isReusable"));
        return new Item(id, legacyId, name, reusable);
    }

    /**
     * Figures out which puzzle type we are dealing with and builds it.
     */
    private Puzzle parsePuzzle(JSONObject puzzleObj) {
        Long legacyId = asNullableLong(puzzleObj.get("puzzleName"));
        UUID id = deriveUuid("puzzle", legacyId);
        String name = (String) puzzleObj.getOrDefault("name", legacyId == null ? "Puzzle" : "Puzzle " + legacyId);
        String description = (String) puzzleObj.getOrDefault("description", "");
        String reward = (String) puzzleObj.getOrDefault("reward", "");
        boolean solved = Boolean.TRUE.equals(puzzleObj.get("solved"));
        PuzzleType type = PuzzleType.fromString((String) puzzleObj.get("type"));

        return switch (type) {
            case MULTIPLE_CHOICE -> {
                List<String> options = readStringList((JSONArray) puzzleObj.get("options"));
                String correctOption = (String) puzzleObj.getOrDefault("correctOption", "");
                yield new MultipleChoicePuzzle(id, legacyId, name, description, reward, options, correctOption, solved);
            }
            case SEQUENCE -> {
                List<String> sequence = readStringList((JSONArray) puzzleObj.get("sequence"));
                yield new SequencePuzzle(id, legacyId, name, description, reward, sequence, solved);
            }
            case RIDDLE -> {
                String riddle = (String) puzzleObj.getOrDefault("riddle", description);
                String answer = (String) puzzleObj.getOrDefault("answer", "");
                yield new RiddlePuzzle(id, legacyId, name, description, reward, riddle, answer, solved);
            }
            case CODE_LOCK -> {
                String code = (String) puzzleObj.getOrDefault("code", puzzleObj.getOrDefault("solution", ""));
                yield new CodeLockPuzzle(id, legacyId, name, description, reward, code, solved);
            }
            case WRITE_IN -> {
                String answer = (String) puzzleObj.getOrDefault("correctAnswer", puzzleObj.getOrDefault("solution", ""));
                yield new WriteInPuzzle(id, legacyId, name, description, reward, answer, solved);
            }
        };
    }

    private List<String> readStringList(JSONArray array) {
        List<String> values = new ArrayList<>();
        if (array != null) {
            for (Object element : array) {
                if (element != null) {
                    values.add(element.toString());
                }
            }
        }
        return values;
    }

    private Set<UUID> readUuidSet(JSONArray array) {
        Set<UUID> values = new HashSet<>();
        if (array == null) {
            return values;
        }
        for (Object element : array) {
            if (element instanceof String str && !str.isBlank()) {
                try {
                    values.add(UUID.fromString(str.trim()));
                    continue;
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (element instanceof Number number) {
                values.add(deriveUuid("puzzle", number));
            }
        }
        return values;
    }

    private PlayerList parsePlayers(JSONArray playersArray) {
        PlayerList players = new PlayerList();
        if (playersArray != null) {
            for (Object obj : playersArray) {
                if (obj instanceof JSONObject playerObj) {
                    players.add(parsePlayer(playerObj));
                }
            }
        }
        return players;
    }

    private Player parsePlayer(JSONObject playerObj) {
        Long legacyId = asNullableLong(playerObj.get("playerID"));
        UUID id = deriveUuid("player", legacyId);
        String name = (String) playerObj.getOrDefault("name", "Unknown");
        String email = (String) playerObj.getOrDefault("email", "");
        String avatar = (String) playerObj.get("avatar");
        int currentScore = asNumber(playerObj.get("currentScore")).intValue();

        ItemList inventory = new ItemList();
        JSONArray itemsArray = (JSONArray) playerObj.get("items");
        if (itemsArray != null) {
            for (Object item : itemsArray) {
                if (item instanceof JSONObject itemObj) {
                    inventory.add(parseItem(itemObj));
                }
            }
        }

        Statistics stats = parseStatistics((JSONObject) playerObj.get("statistics"));
        Set<UUID> solvedPuzzles = readUuidSet((JSONArray) playerObj.get("solvedPuzzles"));

        return new Player(id, legacyId == null ? null : legacyId.intValue(), name, email, avatar,
                inventory, stats, currentScore, solvedPuzzles);
    }

    private Statistics parseStatistics(JSONObject statsObj) {
        if (statsObj == null) {
            return new Statistics();
        }
        Statistics stats = new Statistics();
        stats.setGamesPlayed(asNumber(statsObj.get("gamesPlayed")).intValue());
        stats.setPuzzlesSolved(asNumber(statsObj.get("puzzlesSolved")).intValue());
        stats.setAverageCompletionTime(parseDuration((String) statsObj.get("avgTime")));
        stats.setGamesWon(asNumber(statsObj.get("gamesWon")).intValue());
        return stats;
    }

    private Number asNumber(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private Long asNullableLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Duration parseDuration(String input) {
        if (input == null || input.isBlank()) {
            return Duration.ZERO;
        }
        String[] parts = input.trim().split(":");
        try {
            if (parts.length == 3) {
                long hours = Long.parseLong(parts[0]);
                long minutes = Long.parseLong(parts[1]);
                long seconds = Long.parseLong(parts[2]);
                return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            } else if (parts.length == 2) {
                long minutes = Long.parseLong(parts[0]);
                long seconds = Long.parseLong(parts[1]);
                return Duration.ofMinutes(minutes).plusSeconds(seconds);
            } else if (parts.length == 1) {
                return Duration.ofSeconds(Long.parseLong(parts[0]));
            }
        } catch (NumberFormatException ignored) {
        }
        return Duration.ZERO;
    }

    private UUID deriveUuid(String prefix, Number id) {
        if (id == null) {
            return UUID.randomUUID();
        }
        String seed = prefix + "-" + id.longValue();
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
