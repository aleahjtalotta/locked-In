package com.classes;

/**
 * Enumerates the escape-room difficulty presets. The loader defaults to
 * {@link #MEDIUM} when the persisted value is blank or unrecognized so gameplay
 * always has a sane baseline even if the backing data is incomplete.
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
