package com.classes;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class PuzzleListTest {

    @Test
    public void addStoresNonNullPuzzle() {
        PuzzleList puzzleList = new PuzzleList();
        StubPuzzle puzzle = new StubPuzzle(UUID.randomUUID());

        puzzleList.add(puzzle);

        assertEquals(1, puzzleList.size());
        assertTrue(puzzleList.asList().contains(puzzle));
    }

    @Test
    public void addIgnoresNullReference() {
        PuzzleList puzzleList = new PuzzleList();

        puzzleList.add(null);

        assertEquals(0, puzzleList.size());
        assertTrue(puzzleList.asList().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void asListReturnsUnmodifiableView() {
        PuzzleList puzzleList = new PuzzleList();
        puzzleList.add(new StubPuzzle(UUID.randomUUID()));

        List<Puzzle> snapshot = puzzleList.asList();
        snapshot.add(new StubPuzzle(UUID.randomUUID()));
    }

    @Test
    public void findByIdReturnsMatchingPuzzle() {
        UUID id = UUID.randomUUID();
        PuzzleList puzzleList = new PuzzleList();
        StubPuzzle matching = new StubPuzzle(id);
        puzzleList.add(matching);
        puzzleList.add(new StubPuzzle(UUID.randomUUID()));

        assertTrue(puzzleList.findById(id).isPresent());
        assertSame(matching, puzzleList.findById(id).orElseThrow());
    }

    @Test
    public void findByIdReturnsEmptyWhenPuzzleMissing() {
        PuzzleList puzzleList = new PuzzleList();
        puzzleList.add(new StubPuzzle(UUID.randomUUID()));

        assertTrue(puzzleList.findById(UUID.randomUUID()).isEmpty());
    }

    private static final class StubPuzzle extends Puzzle {
        StubPuzzle(UUID id) {
            super(id, null, "Stub", "desc", "reward", PuzzleType.CODE_LOCK, false);
        }

        @Override
        public boolean isCorrectAnswer(String answer) {
            return false;
        }
    }
}
