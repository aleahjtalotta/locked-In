package com.classes;

public enum PuzzleType {
    MULTIPLE_CHOICE,
    WRITE_IN,
    SEQUENCE,
    RIDDLE,
    CODE_LOCK;

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
