package com.lockedin.audio;

import com.classes.CodeLockPuzzle;
import com.classes.MultipleChoicePuzzle;
import com.classes.Puzzle;
import com.classes.RiddlePuzzle;
import com.classes.SequencePuzzle;
import com.classes.WriteInPuzzle;

import java.util.List;
import java.util.StringJoiner;

/**
 * Builds and narrates short puzzle stories.
 */
public final class PuzzleNarration {
    private PuzzleNarration() {
    }

    /**
     * Triggers narration of the provided puzzle on a background thread.
     *
     * @param puzzle puzzle to describe aloud
     */
    public static void narrateAsync(Puzzle puzzle) {
        TextToSpeechService.speakAsync(createStory(puzzle));
    }

    /**
     * Creates a spoken description summarizing the puzzle experience.
     *
     * @param puzzle puzzle to describe; may be {@code null}
     * @return narration text suitable for speech synthesis
     */
    public static String createStory(Puzzle puzzle) {
        if (puzzle == null) {
            return "";
        }
        StringBuilder story = new StringBuilder();
        story.append(puzzle.getName()).append(". ");
        if (!puzzle.getDescription().isBlank()) {
            story.append(puzzle.getDescription().trim()).append(" ");
        }
        story.append(typeSpecificStory(puzzle));
        if (!puzzle.getReward().isBlank()) {
            story.append("Reward at stake: ").append(puzzle.getReward()).append(". ");
        }
        return story.toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Adds narration tailored to the concrete puzzle type.
     *
     * @param puzzle puzzle whose type determines the narration details
     * @return supplemental narration fragment
     */
    private static String typeSpecificStory(Puzzle puzzle) {
        if (puzzle instanceof MultipleChoicePuzzle mc) {
            return narrateMultipleChoice(mc);
        }
        if (puzzle instanceof SequencePuzzle sequence) {
            return narrateSequence(sequence);
        }
        if (puzzle instanceof CodeLockPuzzle) {
            return "Crack the code and unlock the mechanism.";
        }
        if (puzzle instanceof WriteInPuzzle) {
            return "Provide the correct answer to progress.";
        }
        if (puzzle instanceof RiddlePuzzle riddle) {
            return "Listen to the riddle: " + riddle.getRiddle();
        }
        return "";
    }

    /**
     * Builds narration for a multiple-choice puzzle, enumerating the options.
     *
     * @param puzzle multiple-choice puzzle to describe
     * @return narration text that introduces each choice
     */
    private static String narrateMultipleChoice(MultipleChoicePuzzle puzzle) {
        List<String> options = puzzle.getOptions();
        if (options.isEmpty()) {
            return "Pick the correct option.";
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 0; i < options.size(); i++) {
            joiner.add("Option " + (i + 1) + ": " + options.get(i) + ".");
        }
        return joiner.toString();
    }

    /**
     * Builds narration for a sequence puzzle, outlining the expected order.
     *
     * @param puzzle sequence puzzle to describe
     * @return narration text that guides the ordering of steps
     */
    private static String narrateSequence(SequencePuzzle puzzle) {
        List<String> steps = puzzle.getExpectedSequence();
        if (steps.isEmpty()) {
            return "Arrange the sequence in the proper order.";
        }
        StringJoiner joiner = new StringJoiner(" then ");
        for (String step : steps) {
            joiner.add(step);
        }
        return "Order the steps as follows: " + joiner + ".";
    }
}
