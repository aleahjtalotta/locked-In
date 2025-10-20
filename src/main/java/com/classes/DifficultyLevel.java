package com.classes;

/**
 * Represents the difficulty setting for the escape room.
 */
public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD;

    public static DifficultyLevel fromString(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        for (DifficultyLevel level : values()) {
            if (level.name().equalsIgnoreCase(value.trim())) {
                return level;
            }
        }
        return MEDIUM;
    }
}
