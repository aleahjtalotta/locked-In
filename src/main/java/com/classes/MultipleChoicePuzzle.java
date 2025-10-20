package com.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MultipleChoicePuzzle extends Puzzle {
    private final List<String> options;
    private final String correctOption;

    public MultipleChoicePuzzle(UUID id, Long legacyId, String name, String description, String reward,
                                List<String> options, String correctOption, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.MULTIPLE_CHOICE, solved);
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
        this.correctOption = Objects.requireNonNullElse(correctOption, "");
    }

    public List<String> getOptions() {
        return List.copyOf(options);
    }

    public String getCorrectOption() {
        return correctOption;
    }

    @Override
    public boolean isCorrectAnswer(String answer) {
        return correctOption.equalsIgnoreCase(Objects.requireNonNullElse(answer, ""));
    }
}
