package com.classes;

import java.util.Objects;
import java.util.UUID;

public class WriteInPuzzle extends Puzzle {
    private final String correctAnswer;

    public WriteInPuzzle(UUID id, Long legacyId, String name, String description, String reward,
                         String correctAnswer, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.WRITE_IN, solved);
        this.correctAnswer = Objects.requireNonNullElse(correctAnswer, "");
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    @Override
    public boolean isCorrectAnswer(String answer) {
        return correctAnswer.equalsIgnoreCase(Objects.requireNonNullElse(answer, ""));
    }
}
