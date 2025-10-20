package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PuzzleList {
    private final List<Puzzle> puzzles;

    public PuzzleList() {
        this.puzzles = new ArrayList<>();
    }

    public void add(Puzzle puzzle) {
        if (puzzle != null) {
            puzzles.add(puzzle);
        }
    }

    public List<Puzzle> asList() {
        return Collections.unmodifiableList(puzzles);
    }

    public Optional<Puzzle> findById(UUID id) {
        return puzzles.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public int size() {
        return puzzles.size();
    }
}
