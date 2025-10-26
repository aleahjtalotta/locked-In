package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Maintains an in-memory collection of {@link Puzzle} instances within a room.
 * Provides read-only views for consumers while allowing controlled mutation
 * through dedicated helpers.
 */
public class PuzzleList {
    private final List<Puzzle> puzzles;

    /**
     * Creates an empty list of puzzles.
     */
    public PuzzleList() {
        this.puzzles = new ArrayList<>();
    }

    /**
     * Adds a puzzle to the collection when the provided instance is non-null.
     *
     * @param puzzle puzzle to store; ignored when {@code null}
     */
    public void add(Puzzle puzzle) {
        if (puzzle != null) {
            puzzles.add(puzzle);
        }
    }

    /**
     * Returns an unmodifiable view of the underlying puzzles.
     *
     * @return immutable list mirroring the current collection
     */
    public List<Puzzle> asList() {
        return Collections.unmodifiableList(puzzles);
    }

    /**
     * Retrieves a puzzle by its unique identifier.
     *
     * @param id {@link UUID} that identifies the target puzzle
     * @return optional containing the matching puzzle or empty when not found
     */
    public Optional<Puzzle> findById(UUID id) {
        return puzzles.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /**
     * Reports the number of tracked puzzles.
     *
     * @return total number of active puzzles
     */
    public int size() {
        return puzzles.size();
    }
}
