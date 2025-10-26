package com.lockedin.audio;

import com.classes.Item;
import com.classes.Puzzle;
import com.classes.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Builds and narrates short room introductions when the player enters a space.
 * Tried to write the docs like a student giving a quick walkthrough.
 */
public final class RoomNarration {
    private RoomNarration() {
    }

    /**
     * Sends the narration to the text-to-speech service on another thread.
     *
     * @param room room we are talking about
     */
    public static void narrateAsync(Room room) {
        TextToSpeechService.speakAsync(createStory(room));
    }

    /**
     * Puts together a short story based on what is inside the room.
     *
     * @param room room to describe; null just returns an empty string
     * @return human-readable narration with items and puzzles that matter
     */
    public static String createStory(Room room) {
        if (room == null) {
            return "";
        }

        StringBuilder story = new StringBuilder();
        story.append("You enter ");
        if (room.getLegacyId() != null) {
            story.append("room ").append(room.getLegacyId());
        } else {
            story.append("a new chamber");
        }
        story.append(". ");

        appendItemDetails(room, story);
        appendPuzzleDetails(room, story);

        return story.toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Adds lines for any items we can see in the room.
     *
     * @param room current room context
     * @param story mutable story we are building
     */
    private static void appendItemDetails(Room room, StringBuilder story) {
        List<Item> items = room.getItems();
        if (items.isEmpty()) {
            story.append("The room offers no obvious items. ");
            return;
        }

        List<String> names = items.stream()
                .map(Item::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
        if (names.isEmpty()) {
            story.append("The room offers no obvious items. ");
            return;
        }

        story.append("You notice ");
        story.append(formatList(names));
        story.append(". ");
    }

    /**
     * Adds puzzle information, focusing on the ones that still need solving.
     *
     * @param room current room context
     * @param story mutable story we are building
     */
    private static void appendPuzzleDetails(Room room, StringBuilder story) {
        List<Puzzle> puzzles = room.getPuzzles();
        if (puzzles.isEmpty()) {
            story.append("There are no puzzles to solve here yet.");
            return;
        }

        List<String> unsolved = puzzles.stream()
                .filter(puzzle -> !puzzle.isSolved())
                .map(Puzzle::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));

        if (unsolved.isEmpty()) {
            story.append("Every puzzle in this room has already been solved.");
            return;
        }

        if (unsolved.size() == 1) {
            story.append("One challenge awaits: ");
            story.append(unsolved.get(0));
            story.append(".");
            return;
        }

        story.append("Several challenges await: ");
        story.append(formatList(unsolved));
        story.append(".");
    }

    /**
     * Turns a list of strings into a friendly sentence fragment.
     *
     * @param items names to join together
     * @return readable list like "a, b, and c"
     */
    private static String formatList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        if (items.size() == 1) {
            return items.get(0);
        }

        if (items.size() == 2) {
            return items.get(0) + " and " + items.get(1);
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < items.size() - 1; i++) {
            joiner.add(items.get(i));
        }
        return joiner + ", and " + items.get(items.size() - 1);
    }
}
