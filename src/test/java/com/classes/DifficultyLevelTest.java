package com.classes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DifficultyLevelTest {

    @Test
    public void fromStringMatchesEnumNameCaseInsensitive() {
        assertEquals(DifficultyLevel.EASY, DifficultyLevel.fromString("easy"));
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString("MeDiUm"));
        assertEquals(DifficultyLevel.HARD, DifficultyLevel.fromString("HARD"));
    }

    @Test
    public void fromStringTrimsWhitespaceBeforeMatching() {
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString("  medium  "));
        assertEquals(DifficultyLevel.EASY, DifficultyLevel.fromString("\teasy\n"));
    }

    @Test
    public void fromStringReturnsMediumWhenInputBlank() {
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString(""));
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString("   "));
    }

    @Test
    public void fromStringReturnsMediumWhenInputNull() {
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString(null));
    }

    @Test
    public void fromStringReturnsMediumForUnknownValues() {
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString("expert"));
        assertEquals(DifficultyLevel.MEDIUM, DifficultyLevel.fromString("123"));
    }
}
