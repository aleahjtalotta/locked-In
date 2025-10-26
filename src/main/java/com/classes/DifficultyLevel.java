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

    /**
     * Converts a string representation to a {@link DifficultyLevel}, defaulting
     * to {@link #MEDIUM} when the input is blank or does not match a known
     * level.
     *
     * @param value difficulty name such as {@code "easy"}, {@code "medium"}, or {@code "hard"}
     * @return matching difficulty level, or {@link #MEDIUM} when the input is null, empty, or invalid
     */
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
