package com.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Basic room model that just keeps track of puzzles and items for the game.
 * Trying to explain it like I would to a classmate so it stays easy to read.
 */
public class Room {
    private final UUID id;
    private final Integer legacyId;
    private final List<Item> items;
    private final List<Puzzle> puzzles;

    /**
     * Builds a room with a required id and an optional legacy id.
     *
     * @param id main UUID for this room (cannot be null)
     * @param legacyId old numeric id if we loaded from older data
     */
    public Room(UUID id, Integer legacyId) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.items = new ArrayList<>();
        this.puzzles = new ArrayList<>();
    }

    /**
     * @return the room's UUID so we always have a unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return optional legacy id when we need to match older saves
     */
    public Integer getLegacyId() {
        return legacyId;
    }

    /**
     * @return copy of the current items so callers do not mess with the list directly
     */
    public List<Item> getItems() {
        return List.copyOf(items);
    }

    /**
     * @return copy of puzzles that live in this room
     */
    public List<Puzzle> getPuzzles() {
        return List.copyOf(puzzles);
    }

    /**
     * Finds the first puzzle that is not solved yet.
     *
     * @return optional puzzle that still needs work
     */
    public Optional<Puzzle> getFirstUnsolvedPuzzle() {
        return puzzles.stream().filter(p -> !p.isSolved()).findFirst();
    }

    /**
     * Searches for a puzzle by its id.
     *
     * @param puzzleId id we are looking for; null just gives an empty optional
     * @return puzzle with that id if it lives in this room
     */
    public Optional<Puzzle> findPuzzle(UUID puzzleId) {
        if (puzzleId == null) {
            return Optional.empty();
        }
        return puzzles.stream().filter(p -> p.getId().equals(puzzleId)).findFirst();
    }

    /**
     * Adds an item to the room if we got a real object.
     *
     * @param item item to stash in the room; ignored when null
     */
    public void addItem(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Adds a puzzle to the room.
     *
     * @param puzzle puzzle we want players to solve; null puzzles are skipped
     */
    public void addPuzzle(Puzzle puzzle) {
        if (puzzle != null) {
            puzzles.add(puzzle);
        }
    }
}
