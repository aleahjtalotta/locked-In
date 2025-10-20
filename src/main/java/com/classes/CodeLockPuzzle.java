package com.classes;

import java.util.Objects;
import java.util.UUID;

public class CodeLockPuzzle extends Puzzle {
    private final String code;

    public CodeLockPuzzle(UUID id, Long legacyId, String name, String description, String reward,
                          String code, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.CODE_LOCK, solved);
        this.code = Objects.requireNonNullElse(code, "");
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean isCorrectAnswer(String answer) {
        return code.equalsIgnoreCase(Objects.requireNonNullElse(answer, ""));
    }
}
