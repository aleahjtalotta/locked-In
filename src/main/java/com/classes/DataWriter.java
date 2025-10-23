package com.classes;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DataWriter {
    private static final String ROOMS_FILE = "rooms.json";
    private static final String USERS_FILE = "users.json";

    private final Path destinationDirectory;

    public DataWriter(Path destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

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
        root.put("rooms", writeRoomsArray(gameSystem.getRooms()));

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

    private JSONArray writeRoomsArray(RoomList rooms) {
        JSONArray array = new JSONArray();
        for (Room room : rooms.asList()) {
            JSONObject roomObj = new JSONObject();
            roomObj.put("roomID", valueOrFallback(room.getLegacyId(), room.getId()));
            roomObj.put("items", writeItems(room.getItems()));
            roomObj.put("puzzles", writePuzzles(room.getPuzzles()));
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

    private JSONArray writePuzzles(List<Puzzle> puzzles) {
        JSONArray array = new JSONArray();
        for (Puzzle puzzle : puzzles) {
            JSONObject puzzleObj = new JSONObject();
            puzzleObj.put("puzzleName", valueOrFallback(puzzle.getLegacyId(), puzzle.getId()));
            puzzleObj.put("name", puzzle.getName());
            puzzleObj.put("description", puzzle.getDescription());
            puzzleObj.put("reward", puzzle.getReward());
            puzzleObj.put("type", puzzle.getType().name());
            puzzleObj.put("solved", puzzle.isSolved());
            enrichPuzzleByType(puzzle, puzzleObj);
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
}
