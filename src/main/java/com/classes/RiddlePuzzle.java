package com.classes;

import java.util.Objects;
import java.util.UUID;

public class RiddlePuzzle extends Puzzle {
    private final String riddle;
    private final String answer;

    public RiddlePuzzle(UUID id, Long legacyId, String name, String description, String reward,
                        String riddle, String answer, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.RIDDLE, solved);
        this.riddle = Objects.requireNonNullElse(riddle, "");
        this.answer = Objects.requireNonNullElse(answer, "");
    }

    public String getRiddle() {
        return riddle;
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public boolean isCorrectAnswer(String response) {
        return answer.equalsIgnoreCase(Objects.requireNonNullElse(response, ""));
    }
}
