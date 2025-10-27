package com.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Puzzle variant that presents a set of options and validates the selected choice, ignoring case.
 */
public class MultipleChoicePuzzle extends Puzzle {
    private final List<String> options;
    private final String correctOption;

    /**
     * Creates a multiple-choice puzzle with the available options and the correct answer.
     *
     * @param id the unique identifier for the puzzle
     * @param legacyId the legacy identifier if one exists
     * @param name the display name for the puzzle
     * @param description a human-readable description of the puzzle
     * @param reward the reward text earned upon solving
     * @param options the list of presented answer options
     * @param correctOption the option that constitutes the correct answer
     * @param solved whether the puzzle starts in a solved state
     */
    public MultipleChoicePuzzle(UUID id, Long legacyId, String name, String description, String reward,
                                List<String> options, String correctOption, boolean solved) {
        super(id, legacyId, name, description, reward, PuzzleType.MULTIPLE_CHOICE, solved);
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
        this.correctOption = Objects.requireNonNullElse(correctOption, "");
    }

    /**
     * @return an immutable view of the available answer options
     */
    public List<String> getOptions() {
        return List.copyOf(options);
    }

    /**
     * @return the configured correct option
     */
    public String getCorrectOption() {
        return correctOption;
    }

    /**
     * Checks whether the provided answer matches the configured correct option, ignoring case.
     *
     * @param answer the player-submitted answer to validate
     * @return {@code true} when the answer matches the correct option, ignoring case
     */
    @Override
    public boolean isCorrectAnswer(String answer) {
        return correctOption.equalsIgnoreCase(Objects.requireNonNullElse(answer, ""));
    }
}
