package com.classes;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;


import static org.junit.Assert.*;


public class GameFacadeTest {


   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();


   @Test
   public void loginPlayerActivatesExistingPlayer() throws Exception {
       GameFacade facade = newFacade();
       Player existing = createPlayer("Alex Morgan", "alex@example.com");
       facade.getGameSystem().getPlayers().add(existing);


       boolean loggedIn = facade.loginPlayer(existing.getId());


       assertTrue(loggedIn);
       assertTrue(facade.getActivePlayer().isPresent());
       assertEquals(existing.getId(), facade.getActivePlayer().get().getId());
       assertEquals(existing.getId(), facade.getGameSystem().getProgress().getActivePlayerId());
   }


   @Test
   public void createAccountRejectsDuplicateEmail() throws Exception {
       GameFacade facade = newFacade();
       Player existing = createPlayer("Dana Stone", "dana@example.com");
       facade.getGameSystem().getPlayers().add(existing);


       try {
           facade.createAccount("Dana Clone", "dana@example.com", null);
           fail("Expected duplicate email to be rejected.");
       } catch (IllegalArgumentException ex) {
           assertTrue(ex.getMessage().contains("Dana Stone"));
       }
   }


   @Test
   public void submitAnswerAwardsPointsAndAdvancesProgress() throws Exception {
       GameFacade facade = newFacade();
       Player player = createPlayer("Morgan Grey", "morgan@example.com");
       facade.getGameSystem().getPlayers().add(player);


       WriteInPuzzle puzzle = createWriteInPuzzle("open sesame");
       Room room = createRoom();
       registerRoomWithPuzzle(facade, room, puzzle);


       assertTrue(facade.loginPlayer(player.getId()));


       boolean solved = facade.submitAnswer(puzzle.getId(), "open sesame");


       assertTrue(solved);
       assertTrue(puzzle.isSolved());
       assertTrue(facade.getGameSystem().getProgress().getSolvedPuzzleIds().contains(puzzle.getId()));
       assertEquals(5, facade.getActivePlayer().get().getCurrentScore());
       assertTrue(facade.getLeaderboard().getScores().stream()
               .anyMatch(entry -> entry.getPlayerName().equals(player.getName()) && entry.getScore() == 5));
       assertNull(facade.getGameSystem().getProgress().getCurrentRoomId());
   }


   @Test
   public void submitAnswerReturnsFalseForIncorrectAnswer() throws Exception {
       GameFacade facade = newFacade();
       Player player = createPlayer("Riley Shaw", "riley@example.com");
       facade.getGameSystem().getPlayers().add(player);


       WriteInPuzzle puzzle = createWriteInPuzzle("solution");
       Room room = createRoom();
       registerRoomWithPuzzle(facade, room, puzzle);


       assertTrue(facade.loginPlayer(player.getId()));


       boolean solved = facade.submitAnswer(puzzle.getId(), "wrong");


       assertFalse(solved);
       assertFalse(puzzle.isSolved());
       assertFalse(facade.getGameSystem().getProgress().getSolvedPuzzleIds().contains(puzzle.getId()));
       assertEquals(0, facade.getActivePlayer().get().getCurrentScore());
       assertTrue(facade.getLeaderboard().getScores().isEmpty());
   }


   @Test
   public void useHintPenalizesActivePlayerAndConsumesHint() throws Exception {
       GameFacade facade = newFacade();
       Player player = createPlayer("Jamie Ivy", "jamie@example.com");
       player.addScore(3);
       facade.getGameSystem().getPlayers().add(player);
       facade.getGameSystem().getHints().addHint(new Hint(UUID.randomUUID(), 1L, "Try the red key"));


       assertTrue(facade.loginPlayer(player.getId()));


       Optional<Hint> dispensed = facade.useHint();


       assertTrue(dispensed.isPresent());
       assertEquals("Try the red key", dispensed.get().getText());
       assertEquals(2, facade.getActivePlayer().get().getCurrentScore());
       assertTrue(facade.getLeaderboard().getScores().stream()
               .anyMatch(entry -> entry.getPlayerName().equals(player.getName()) && entry.getScore() == 2));
   }


   @Test
   public void useHintReturnsEmptyWhenNoHintsRemain() throws Exception {
       GameFacade facade = newFacade();
       Player player = createPlayer("Casey Dell", "casey@example.com");
       player.addScore(4);
       facade.getGameSystem().getPlayers().add(player);


       assertTrue(facade.loginPlayer(player.getId()));


       Optional<Hint> dispensed = facade.useHint();


       assertFalse(dispensed.isPresent());
       assertEquals(4, facade.getActivePlayer().get().getCurrentScore());
       assertTrue(facade.getLeaderboard().getScores().isEmpty());
   }


   @Test
   public void startTimerCountdownInitializesAndStartsTimer() throws Exception {
       GameFacade facade = newFacade();


       facade.startTimerCountdown();


       Timer timer = facade.getGameSystem().getTimer();
       assertEquals(Duration.ofMinutes(15), timer.getTotalTime());
       assertTrue(timer.isRunning());
       assertTrue(timer.getRemaining().compareTo(Duration.ZERO) > 0);
   }


   @Test
   public void pauseTimerCountdownResetsDefaultDurationAfterExpiry() throws Exception {
       GameFacade facade = newFacade();


       facade.startTimerCountdown();
       Timer timer = facade.getGameSystem().getTimer();
       timer.setRemaining(Duration.ZERO);


       facade.pauseTimerCountdown();


       timer.setTotalTime(Duration.ofMinutes(10));
       timer.reset();
       facade.startTimerCountdown();


       assertEquals(Duration.ofMinutes(15), timer.getTotalTime());
       assertTrue(timer.isRunning());
   }


   private GameFacade newFacade() throws IOException {
       File directory = temporaryFolder.newFolder("game-" + UUID.randomUUID());
       return new GameFacade(directory.getAbsolutePath());
   }


   private Player createPlayer(String name, String email) {
       return new Player(UUID.randomUUID(), null, name, email, null,
               new ItemList(), new Statistics(), 0, Collections.emptySet());
   }


   private WriteInPuzzle createWriteInPuzzle(String answer) {
       return new WriteInPuzzle(UUID.randomUUID(), 1L, "Cipher Lock", "Enter the code", "Unlocked", answer, false);
   }


   private Room createRoom() {
       return new Room(UUID.randomUUID(), 1);
   }


   private void registerRoomWithPuzzle(GameFacade facade, Room room, Puzzle puzzle) {
       room.addPuzzle(puzzle);
       facade.getGameSystem().getRooms().add(room);
       facade.getGameSystem().getPuzzles().add(puzzle);
   }
}
