package com.classes;


import org.junit.Test;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.Assert.*;


public class RoomTest {


   @Test
   public void addItemStoresNonNullItems() {
       Room room = new Room(UUID.randomUUID(), 10);
       Item key = new Item(UUID.randomUUID(), 5L, "Old Key", false);


       room.addItem(key);


       List<Item> items = room.getItems();
       assertEquals(1, items.size());
       assertEquals("Old Key", items.get(0).getName());
   }


   @Test
   public void addItemIgnoresNullReferences() {
       Room room = new Room(UUID.randomUUID(), 11);


       room.addItem(null);


       assertTrue(room.getItems().isEmpty());
   }


   @Test
   public void addPuzzleStoresNonNullPuzzle() {
       Room room = new Room(UUID.randomUUID(), 12);
       Puzzle puzzle = new WriteInPuzzle(UUID.randomUUID(), 3L, "Password", "Enter code", "Item unlocked", "open", false);


       room.addPuzzle(puzzle);


       List<Puzzle> puzzles = room.getPuzzles();
       assertEquals(1, puzzles.size());
       assertEquals("Password", puzzles.get(0).getName());
   }


   @Test
   public void addPuzzleIgnoresNullReferences() {
       Room room = new Room(UUID.randomUUID(), 13);


       room.addPuzzle(null);


       assertTrue(room.getPuzzles().isEmpty());
   }


   @Test
   public void findPuzzleReturnsMatchingPuzzle() {
       Room room = new Room(UUID.randomUUID(), 20);
       WriteInPuzzle puzzle = new WriteInPuzzle(UUID.randomUUID(), 2L, "Cipher", "Solve", "Reward", "answer", false);
       room.addPuzzle(puzzle);


       Optional<Puzzle> found = room.findPuzzle(puzzle.getId());


       assertTrue(found.isPresent());
       assertEquals(puzzle.getId(), found.get().getId());
   }


   @Test
   public void findPuzzleReturnsEmptyForNullId() {
       Room room = new Room(UUID.randomUUID(), 21);
       WriteInPuzzle puzzle = new WriteInPuzzle(UUID.randomUUID(), 4L, "Cipher", "Solve", "Reward", "answer", false);
       room.addPuzzle(puzzle);


       Optional<Puzzle> found = room.findPuzzle(null);


       assertFalse(found.isPresent());
   }


   @Test
   public void getFirstUnsolvedPuzzleReturnsFirstUnsolved() {
       Room room = new Room(UUID.randomUUID(), 30);
       WriteInPuzzle solved = new WriteInPuzzle(UUID.randomUUID(), 5L, "Solved", "Done", "None", "done", true);
       WriteInPuzzle unsolved = new WriteInPuzzle(UUID.randomUUID(), 6L, "Open", "Not solved", "Prize", "open", false);
       room.addPuzzle(solved);
       room.addPuzzle(unsolved);


       Optional<Puzzle> first = room.getFirstUnsolvedPuzzle();


       assertTrue(first.isPresent());
       assertEquals(unsolved.getId(), first.get().getId());
   }


   @Test
   public void getFirstUnsolvedPuzzleReturnsEmptyWhenAllSolved() {
       Room room = new Room(UUID.randomUUID(), 31);
       WriteInPuzzle solved = new WriteInPuzzle(UUID.randomUUID(), 7L, "Solved", "Done", "None", "done", true);
       room.addPuzzle(solved);


       Optional<Puzzle> first = room.getFirstUnsolvedPuzzle();


       assertFalse(first.isPresent());
   }
}




