package com.classes;

/**
 * Enumerates the supported varieties of puzzles available in the escape-room
 * game. The set mirrors the specialized {@link Puzzle} subclasses so that the
 * rest of the system can branch on a stable, serializable value.
 */
public enum PuzzleType {
    MULTIPLE_CHOICE,
    WRITE_IN,
    SEQUENCE,
    RIDDLE,
    CODE_LOCK;

    /**
     * Converts a string read from storage or user input into a {@link PuzzleType}.
     * The comparison ignores case and accepts a few legacy aliases so older data
     * files remain compatible.
     *
     * @param type raw value describing a puzzle type; may be {@code null}
     * @return resolved puzzle type, defaulting to {@link #WRITE_IN} when the input
     *         is {@code null} or unrecognized
     */
    public static PuzzleType fromString(String type) {
        if (type == null) {
            return WRITE_IN;
        }
        return switch (type.trim().toUpperCase()) {
            case "MULTIPLECHOICE", "MULTIPLE_CHOICE" -> MULTIPLE_CHOICE;
            case "WRITEIN", "WRITE_IN", "WRITTENPUZZLE", "WRITTEN" -> WRITE_IN;
            case "SEQUENCE", "ORDERING" -> SEQUENCE;
            case "RIDDLE" -> RIDDLE;
            case "CODE", "CODE_LOCK", "PASSWORD" -> CODE_LOCK;
            default -> WRITE_IN;
        };
    }
}
