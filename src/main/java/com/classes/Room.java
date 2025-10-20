package com.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Room {
    private final UUID id;
    private final Integer legacyId;
    private final List<Item> items;
    private final List<Puzzle> puzzles;

    public Room(UUID id, Integer legacyId) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = legacyId;
        this.items = new ArrayList<>();
        this.puzzles = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public Integer getLegacyId() {
        return legacyId;
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public List<Puzzle> getPuzzles() {
        return List.copyOf(puzzles);
    }

    public Optional<Puzzle> getFirstUnsolvedPuzzle() {
        return puzzles.stream().filter(p -> !p.isSolved()).findFirst();
    }

    public Optional<Puzzle> findPuzzle(UUID puzzleId) {
        if (puzzleId == null) {
            return Optional.empty();
        }
        return puzzles.stream().filter(p -> p.getId().equals(puzzleId)).findFirst();
    }

    public void addItem(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    public void addPuzzle(Puzzle puzzle) {
        if (puzzle != null) {
            puzzles.add(puzzle);
        }
    }
}
