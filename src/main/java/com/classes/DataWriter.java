package com.classes;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Persists the current escape-room state back into the JSON files used by the
 * application. This writer produces a pair of documents ({@code rooms.json} and
 * {@code users.json}) whose shape mirrors the structure that
 * {@link DataLoader} expects when rebuilding a {@link GameSystem}.
 * <p>
 * Instances are mutable only in that they remember the destination directory;
 * call {@link #saveGame(GameSystem)} whenever the in-memory state should be
 * flushed to disk.
 * </p>
 */
public class DataWriter {
    private static final String ROOMS_FILE = "rooms.json";
    private static final String USERS_FILE = "users.json";

    private final Path destinationDirectory;

    /**
     * Creates a writer that emits JSON files into the supplied directory. The
     * directory is created on demand when {@link #saveGame(GameSystem)} runs.
     *
     * @param destinationDirectory folder that should contain the generated JSON
     */
    public DataWriter(Path destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    /**
     * Serializes the provided game system to disk. The existing game state is
     * broken into its rooms, leaderboard, hints, players, and timer sections
     * and written to {@code rooms.json} and {@code users.json}.
     *
     * @param gameSystem current in-memory game state; must not be {@code null}
     * @return {@code true} when the save completed successfully, {@code false}
     *         when an {@link IOException} occurred
     */
    public boolean saveGame(GameSystem gameSystem) {
        Objects.requireNonNull(gameSystem, "gameSystem");
        try {
            Files.createDirectories(destinationDirectory);
            writeRooms(gameSystem);
            writeUsers(gameSystem.getPlayers());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void writeRooms(GameSystem gameSystem) throws IOException {
        JSONObject root = new JSONObject();
        root.put("gameSystemID", valueOrFallback(gameSystem.getLegacyId(), gameSystem.getId()));
        root.put("currentDifficulty", gameSystem.getDifficulty().name());
        root.put("timer", writeTimer(gameSystem.getTimer()));
        root.put("hints", writeHints(gameSystem.getHints()));
        root.put("leaderboard", writeLeaderboard(gameSystem.getLeaderboard()));

        Map<Long, PuzzleHint> puzzleHints = loadExistingPuzzleHints();
        root.put("rooms", writeRoomsArray(gameSystem.getRooms(), puzzleHints));

        writeJson(destinationDirectory.resolve(ROOMS_FILE), root);
    }

    private void writeUsers(PlayerList players) throws IOException {
        JSONArray usersArray = new JSONArray();
        for (Player player : players.asList()) {
            JSONObject obj = new JSONObject();
            obj.put("playerID", valueOrFallback(player.getLegacyId(), player.getId()));
            obj.put("name", player.getName());
            obj.put("email", player.getEmail());
            obj.put("avatar", player.getAvatar());
            obj.put("currentScore", player.getCurrentScore());
            obj.put("items", writeItems(player.getInventory().asList()));
            obj.put("statistics", writeStatistics(player.getStatistics()));
            obj.put("solvedPuzzles", writeSolvedPuzzles(player));
            usersArray.add(obj);
        }
        writeJson(destinationDirectory.resolve(USERS_FILE), usersArray);
    }

    private JSONObject writeTimer(Timer timer) {
        JSONObject obj = new JSONObject();
        if (timer != null) {
            obj.put("timerID", 0);
            obj.put("totalTime", formatDuration(timer.getTotalTime()));
            obj.put("timeRemaining", formatDuration(timer.getRemaining()));
        }
        return obj;
    }

    private JSONArray writeHints(Hints hints) {
        JSONArray array = new JSONArray();
        if (hints != null) {
            for (Hint hint : hints.getRemainingHints()) {
                JSONObject obj = new JSONObject();
                obj.put("hintID", valueOrFallback(hint.getLegacyId(), hint.getId()));
                obj.put("hintText", hint.getText());
                array.add(obj);
            }
        }
        return array;
    }

    private JSONObject writeLeaderboard(Leaderboard leaderboard) {
        JSONObject obj = new JSONObject();
        JSONArray scores = new JSONArray();
        if (leaderboard != null) {
            for (ScoreEntry entry : leaderboard.getScores()) {
                JSONObject scoreObj = new JSONObject();
                scoreObj.put("scoreEntryID", valueOrFallback(entry.getLegacyId(), entry.getId()));
                scoreObj.put("playerName", entry.getPlayerName());
                scoreObj.put("score", entry.getScore());
                scoreObj.put("completionTime", formatDuration(entry.getCompletionTime()));
                scores.add(scoreObj);
            }
        }
        obj.put("scores", scores);
        return obj;
    }

    private JSONArray writeRoomsArray(RoomList rooms, Map<Long, PuzzleHint> puzzleHints) {
        JSONArray array = new JSONArray();
        for (Room room : rooms.asList()) {
            JSONObject roomObj = new JSONObject();
            roomObj.put("roomID", valueOrFallback(room.getLegacyId(), room.getId()));
            roomObj.put("items", writeItems(room.getItems()));
            roomObj.put("puzzles", writePuzzles(room.getPuzzles(), puzzleHints));
            array.add(roomObj);
        }
        return array;
    }

    private JSONArray writeItems(List<Item> items) {
        JSONArray array = new JSONArray();
        for (Item item : items) {
            JSONObject itemObj = new JSONObject();
            itemObj.put("itemID", valueOrFallback(item.getLegacyId(), item.getId()));
            itemObj.put("itemName", item.getName());
            itemObj.put("isReusable", item.isReusable());
            array.add(itemObj);
        }
        return array;
    }

    private JSONArray writePuzzles(List<Puzzle> puzzles, Map<Long, PuzzleHint> puzzleHints) {
        JSONArray array = new JSONArray();
        for (Puzzle puzzle : puzzles) {
            JSONObject puzzleObj = new JSONObject();
            Long legacyId = puzzle.getLegacyId();
            puzzleObj.put("puzzleName", valueOrFallback(legacyId, puzzle.getId()));
            puzzleObj.put("name", puzzle.getName());
            puzzleObj.put("description", puzzle.getDescription());
            puzzleObj.put("reward", puzzle.getReward());
            puzzleObj.put("type", puzzle.getType().name());
            puzzleObj.put("solved", puzzle.isSolved());
            enrichPuzzleByType(puzzle, puzzleObj);
            includePuzzleHintMetadata(puzzleObj, legacyId, puzzleHints);
            array.add(puzzleObj);
        }
        return array;
    }

    private void enrichPuzzleByType(Puzzle puzzle, JSONObject target) {
        if (puzzle instanceof MultipleChoicePuzzle mc) {
            JSONArray options = new JSONArray();
            options.addAll(mc.getOptions());
            target.put("options", options);
            target.put("correctOption", mc.getCorrectOption());
        } else if (puzzle instanceof WriteInPuzzle writeIn) {
            target.put("correctAnswer", writeIn.getCorrectAnswer());
        } else if (puzzle instanceof SequencePuzzle sequence) {
            JSONArray expected = new JSONArray();
            expected.addAll(sequence.getExpectedSequence());
            target.put("sequence", expected);
        } else if (puzzle instanceof RiddlePuzzle riddle) {
            target.put("riddle", riddle.getRiddle());
            target.put("answer", riddle.getAnswer());
        } else if (puzzle instanceof CodeLockPuzzle codeLock) {
            target.put("code", codeLock.getCode());
        }
    }

    private void includePuzzleHintMetadata(JSONObject target,
                                           Long puzzleLegacyId,
                                           Map<Long, PuzzleHint> puzzleHints) {
        if (puzzleLegacyId == null || puzzleHints.isEmpty()) {
            return;
        }
        PuzzleHint hint = puzzleHints.get(puzzleLegacyId);
        if (hint == null || hint.hintText == null || hint.hintText.isBlank()) {
            return;
        }
        if (hint.hintId != null) {
            target.put("hintID", hint.hintId);
        }
        JSONObject hintObj = new JSONObject();
        if (hint.hintId != null) {
            hintObj.put("hintID", hint.hintId);
        }
        hintObj.put("hintText", hint.hintText);
        JSONArray hintsArray = new JSONArray();
        hintsArray.add(hintObj);
        target.put("hints", hintsArray);
    }

    private JSONObject writeStatistics(Statistics statistics) {
        JSONObject obj = new JSONObject();
        if (statistics != null) {
            obj.put("gamesPlayed", statistics.getGamesPlayed());
            obj.put("puzzlesSolved", statistics.getPuzzlesSolved());
            obj.put("avgTime", formatDuration(statistics.getAverageCompletionTime()));
            obj.put("gamesWon", statistics.getGamesWon());
        }
        return obj;
    }

    private JSONArray writeSolvedPuzzles(Player player) {
        JSONArray array = new JSONArray();
        if (player != null) {
            for (UUID puzzleId : player.getSolvedPuzzleIds()) {
                array.add(puzzleId.toString());
            }
        }
        return array;
    }

    private Map<Long, PuzzleHint> loadExistingPuzzleHints() {
        Path roomsPath = destinationDirectory.resolve(ROOMS_FILE);
        if (!Files.exists(roomsPath)) {
            return Collections.emptyMap();
        }
        JSONParser parser = new JSONParser();
        try (Reader reader = Files.newBufferedReader(roomsPath, StandardCharsets.UTF_8)) {
            Object parsed = parser.parse(reader);
            if (!(parsed instanceof JSONObject root)) {
                return Collections.emptyMap();
            }
            Map<Long, String> hintTexts = extractHintTextMap((JSONArray) root.get("hints"));
            return extractPuzzleHints((JSONArray) root.get("rooms"), hintTexts);
        } catch (IOException | ParseException e) {
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> extractHintTextMap(JSONArray hintsArray) {
        if (hintsArray == null) {
            return Collections.emptyMap();
        }
        Map<Long, String> hints = new HashMap<>();
        for (Object entry : hintsArray) {
            if (entry instanceof JSONObject hintObj) {
                Long id = asLong(hintObj.get("hintID"));
                if (id != null) {
                    Object text = hintObj.get("hintText");
                    hints.put(id, text == null ? "" : text.toString());
                }
            }
        }
        return hints;
    }

    private Map<Long, PuzzleHint> extractPuzzleHints(JSONArray roomsArray, Map<Long, String> fallbackById) {
        if (roomsArray == null) {
            return Collections.emptyMap();
        }
        Map<Long, PuzzleHint> puzzleHints = new HashMap<>();
        for (Object roomObj : roomsArray) {
            if (!(roomObj instanceof JSONObject room)) {
                continue;
            }
            JSONArray puzzles = (JSONArray) room.get("puzzles");
            if (puzzles == null) {
                continue;
            }
            for (Object puzzleObj : puzzles) {
                if (!(puzzleObj instanceof JSONObject puzzle)) {
                    continue;
                }
                Long puzzleId = asLong(puzzle.get("puzzleName"));
                if (puzzleId == null) {
                    continue;
                }
                PuzzleHint hint = resolvePuzzleHint(puzzle, fallbackById);
                if (hint != null) {
                    puzzleHints.put(puzzleId, hint);
                }
            }
        }
        return puzzleHints;
    }

    private PuzzleHint resolvePuzzleHint(JSONObject puzzle, Map<Long, String> fallbackById) {
        JSONArray hintsArray = (JSONArray) puzzle.get("hints");
        String text = firstHintText(hintsArray);
        Long hintId = asLong(puzzle.get("hintID"));
        if ((text == null || text.isBlank()) && hintId != null) {
            text = fallbackById.get(hintId);
        }
        if (hintId == null && hintsArray != null && !hintsArray.isEmpty()) {
            Object first = hintsArray.get(0);
            if (first instanceof JSONObject hintObj) {
                hintId = asLong(hintObj.get("hintID"));
            }
        }
        if (text == null || text.isBlank()) {
            return null;
        }
        return new PuzzleHint(hintId, text);
    }

    private String firstHintText(JSONArray hintsArray) {
        if (hintsArray == null || hintsArray.isEmpty()) {
            return null;
        }
        Object first = hintsArray.get(0);
        if (first instanceof JSONObject hintObj) {
            Object text = hintObj.get("hintText");
            if (text != null) {
                return text.toString();
            }
        }
        return null;
    }

    private void writeJson(Path path, JSONObject content) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            content.writeJSONString(writer);
        }
    }

    private void writeJson(Path path, JSONArray content) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            content.writeJSONString(writer);
        }
    }

    private long valueOrFallback(Long legacyId, UUID id) {
        if (legacyId != null) {
            return legacyId;
        }
        return fallbackId(id);
    }

    private long valueOrFallback(Integer legacyId, UUID id) {
        if (legacyId != null) {
            return legacyId;
        }
        return fallbackId(id);
    }

    private long fallbackId(UUID id) {
        long value = id.getLeastSignificantBits();
        if (value < 0) {
            value = -value;
        }
        return value;
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return "00:00:00";
        }
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private Long asLong(Object value) {
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

    private static final class PuzzleHint {
        private final Long hintId;
        private final String hintText;

        private PuzzleHint(Long hintId, String hintText) {
            this.hintId = hintId;
            this.hintText = hintText;
        }
    }
}
