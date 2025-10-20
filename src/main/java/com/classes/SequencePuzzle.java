package com.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SequencePuzzle extends Puzzle {
    private final List<String> expectedSequence;

    public SequencePuzzle(UUID id, Long legacyId, String name, String description, String reward,
                          List<String> expectedSequence, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.SEQUENCE, solved);
        this.expectedSequence = expectedSequence == null ? new ArrayList<>() : new ArrayList<>(expectedSequence);
    }

    public List<String> getExpectedSequence() {
        return List.copyOf(expectedSequence);
    }

    @Override
    public boolean isCorrectAnswer(String answer) {
        if (answer == null) {
            return false;
        }
        String normalized = answer.trim().replaceAll("\\s+", " ");
        String expected = String.join(" ", expectedSequence).trim().replaceAll("\\s+", " ");
        return Objects.equals(normalized.toLowerCase(), expected.toLowerCase());
    }
}
