package com.lockedin.ui;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Supplies the hint text assigned to each puzzle by reading the static
 * {@code rooms.json} file generated for the UI build.
 */
public final class HintProvider {
    private static final Path ROOMS_FILE = Paths.get("JSON", "rooms.json");
    private static final Object LOCK = new Object();

    private static Map<Long, String> cachedPuzzleHints;

    private HintProvider() {
    }

    /**
     * @param puzzleLegacyId legacy identifier defined in {@code rooms.json}
     * @return hint text mapped to that puzzle when available
     */
    public static Optional<String> getHintForPuzzle(Long puzzleLegacyId) {
        if (puzzleLegacyId == null) {
            return Optional.empty();
        }
        synchronized (LOCK) {
            Map<Long, String> hints = ensureHints();
            if (hints == null) {
                return Optional.empty();
            }
            String text = hints.get(puzzleLegacyId);
            if (text == null || text.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(text);
        }
    }

    private static Map<Long, String> ensureHints() {
        if (cachedPuzzleHints == null) {
            cachedPuzzleHints = loadHints();
        }
        return cachedPuzzleHints;
    }

    private static Map<Long, String> loadHints() {
        JSONParser parser = new JSONParser();
        try (Reader reader = Files.newBufferedReader(ROOMS_FILE, StandardCharsets.UTF_8)) {
            Object parsed = parser.parse(reader);
            if (!(parsed instanceof JSONObject root)) {
                return null;
            }
            Map<Long, String> globalHints = extractHintTexts((JSONArray) root.get("hints"));
            return extractPuzzleHints((JSONArray) root.get("rooms"), globalHints);
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    private static Map<Long, String> extractHintTexts(JSONArray hintsArray) {
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

    private static Map<Long, String> extractPuzzleHints(JSONArray roomsArray, Map<Long, String> fallbackById) {
        if (roomsArray == null) {
            return Collections.emptyMap();
        }
        Map<Long, String> puzzleHints = new HashMap<>();
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
                String text = puzzleLevelHint(puzzle);
                if ((text == null || text.isBlank())) {
                    Long hintId = asLong(puzzle.get("hintID"));
                    if (hintId != null) {
                        text = fallbackById.getOrDefault(hintId, "");
                    }
                }
                if (text != null && !text.isBlank()) {
                    puzzleHints.put(puzzleId, text);
                }
            }
        }
        return puzzleHints;
    }

    private static String puzzleLevelHint(JSONObject puzzle) {
        Object hintsObj = puzzle.get("hints");
        if (hintsObj instanceof JSONArray array && !array.isEmpty()) {
            Object first = array.get(0);
            if (first instanceof JSONObject hintObj) {
                Object text = hintObj.get("hintText");
                if (text != null) {
                    return text.toString();
                }
            }
        }
        Object direct = puzzle.get("hintText");
        return direct == null ? null : direct.toString();
    }

    private static Long asLong(Object value) {
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

    /** Visible for tests to force reloading hints from disk. */
    static void reset() {
        synchronized (LOCK) {
            cachedPuzzleHints = null;
        }
    }
}
