package com.classes;


import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;


public class LockedInDriver {
   private static final String DEFAULT_DATA_DIR = "JSON";


   public static void main(String[] args) {
       String dataDirectory = args.length > 0 ? args[0] : DEFAULT_DATA_DIR;
       GameFacade game = new GameFacade(dataDirectory);


       if (game.loadGame()) {
           System.out.println("Loaded game data from '" + dataDirectory + "'.");
       } else {
           System.out.println("No saved data found in '" + dataDirectory + "'. Starting with a fresh game state.");
           game.startNewGame();
       }


       Scanner scanner = new Scanner(System.in);
       boolean running = true;


       while (running) {
           printMenu(game, dataDirectory);
           System.out.print("Select an option: ");
           String choice = scanner.nextLine().trim();


           switch (choice) {
               case "1" -> listPlayers(game);
               case "2" -> createPlayer(game, scanner);
               case "3" -> loginPlayer(game, scanner);
               case "4" -> logoutPlayer(game);
               case "5" -> listRooms(game);
               case "6" -> enterRoom(game, scanner);
               case "7" -> showCurrentRoom(game);
               case "8" -> attemptPuzzle(game, scanner);
               case "9" -> requestHint(game);
               case "10" -> showLeaderboard(game);
               case "11" -> saveGame(game);
               case "12" -> reloadGame(game, dataDirectory);
               case "0" -> {
                   running = false;
                   System.out.println("Exiting Locked-In driver. Goodbye!");
               }
               default -> System.out.println("Invalid selection. Please choose a valid option.");
           }
       }
   }


   private static void printMenu(GameFacade game, String dataDirectory) {
       System.out.println();
       System.out.println("--- Locked-In Escape Room ---");
       System.out.println("Active player: " + game.getActivePlayer().map(Player::getName).orElse("none"));
       System.out.println("Current room: " + game.getCurrentRoom().map(LockedInDriver::formatRoomId).orElse("none"));
       Timer timer = game.getGameSystem().getTimer();
       if (timer != null && (!timer.getTotalTime().isZero() || !timer.getRemaining().isZero())) {
           System.out.println("Timer: " + formatDuration(timer.getRemaining()) + " remaining of " + formatDuration(timer.getTotalTime()));
       }
       System.out.println();
       System.out.println(" 1) List players");
       System.out.println(" 2) Create player");
       System.out.println(" 3) Log in as player");
       System.out.println(" 4) Log out");
       System.out.println(" 5) List rooms");
       System.out.println(" 6) Enter room");
       System.out.println(" 7) Show current room details");
       System.out.println(" 8) Attempt puzzle in current room");
       System.out.println(" 9) Use next hint");
       System.out.println("10) Show leaderboard");
       System.out.println("11) Save game");
       System.out.println("12) Reload game data");
       System.out.println(" 0) Exit");
   }


   private static void listPlayers(GameFacade game) {
       List<Player> players = game.getPlayerList().asList();
       if (players.isEmpty()) {
           System.out.println("No players found. Create a player to get started.");
           return;
       }


       System.out.println("Registered players:");
       for (int i = 0; i < players.size(); i++) {
           Player player = players.get(i);
           System.out.printf("%2d) %s (email: %s, score: %d)%n",
                   i + 1,
                   player.getName(),
                   player.getEmail().isBlank() ? "n/a" : player.getEmail(),
                   player.getCurrentScore());
       }
   }


   private static void createPlayer(GameFacade game, Scanner scanner) {
       System.out.print("Enter player name: ");
       String name = scanner.nextLine().trim();


       System.out.print("Enter player email: ");
       String email = scanner.nextLine().trim();


       System.out.print("Enter avatar path (optional): ");
       String avatar = scanner.nextLine().trim();
       if (avatar.isEmpty()) {
           avatar = null;
       }


       try {
           Player player = game.createAccount(name, email, avatar);
           System.out.println("Player created with ID " + player.getId());
       } catch (IllegalArgumentException ex) {
           System.out.println("Unable to create player: " + ex.getMessage());
       }
   }


   private static void loginPlayer(GameFacade game, Scanner scanner) {
       List<Player> players = game.getPlayerList().asList();
       if (players.isEmpty()) {
           System.out.println("No players available to log in.");
           return;
       }


       listPlayers(game);
       System.out.print("Enter player number or email (leave blank to cancel): ");
       String input = scanner.nextLine().trim();
       if (input.isEmpty()) {
           return;
       }


       boolean success = false;
       if (input.matches("\\d+")) {
           try {
               int index = Integer.parseInt(input) - 1;
               if (index >= 0 && index < players.size()) {
                   success = game.loginPlayer(players.get(index).getId());
               }
           } catch (NumberFormatException ignored) {
           }
       } else {
           success = game.login(input);
       }


       if (success) {
           System.out.println("Logged in as " + game.getActivePlayer().map(Player::getName).orElse("unknown player"));
       } else {
           System.out.println("Login failed. Please verify the selection or email address.");
       }
   }


   private static void logoutPlayer(GameFacade game) {
       if (game.getActivePlayer().isPresent()) {
           game.logoutPlayer();
           System.out.println("Player logged out.");
       } else {
           System.out.println("No player is currently logged in.");
       }
   }


   private static void listRooms(GameFacade game) {
       List<Room> rooms = game.getRooms();
       if (rooms.isEmpty()) {
           System.out.println("No rooms are available in the current game data.");
           return;
       }


       System.out.println("Rooms:");
       for (int i = 0; i < rooms.size(); i++) {
           Room room = rooms.get(i);
           long solvedCount = room.getPuzzles().stream().filter(Puzzle::isSolved).count();
           System.out.printf("%2d) Room %s - puzzles solved: %d/%d, items: %d%n",
                   i + 1,
                   formatRoomId(room),
                   solvedCount,
                   room.getPuzzles().size(),
                   room.getItems().size());
       }
   }


   private static void enterRoom(GameFacade game, Scanner scanner) {
       List<Room> rooms = game.getRooms();
       if (rooms.isEmpty()) {
           System.out.println("There are no rooms to enter.");
           return;
       }


       listRooms(game);
       System.out.print("Enter room number (leave blank to cancel): ");
       String input = scanner.nextLine().trim();
       if (input.isEmpty()) {
           return;
       }


       try {
           int index = Integer.parseInt(input) - 1;
           if (index < 0 || index >= rooms.size()) {
               System.out.println("Room selection out of range.");
               return;
           }
           Room selected = rooms.get(index);
           Optional<Room> entered = game.enterRoom(selected.getId());
           if (entered.isPresent()) {
               System.out.println("Entered room " + formatRoomId(entered.get()) + ".");
           } else {
               System.out.println("Unable to enter the selected room.");
           }
       } catch (NumberFormatException ex) {
           System.out.println("Invalid room selection.");
       }
   }


   private static void showCurrentRoom(GameFacade game) {
       Optional<Room> currentRoom = game.getCurrentRoom();
       if (currentRoom.isEmpty()) {
           System.out.println("No room is currently active. Use option 6 to enter a room.");
           return;
       }


       Room room = currentRoom.get();
       System.out.println("Current room: " + formatRoomId(room));
       if (room.getItems().isEmpty()) {
           System.out.println("Items: none");
       } else {
           System.out.println("Items:");
           room.getItems().forEach(item ->
                   System.out.println(" - " + item.getName() + (item.isReusable() ? " (reusable)" : "")));
       }


       List<Puzzle> puzzles = room.getPuzzles();
       if (puzzles.isEmpty()) {
           System.out.println("This room does not contain any puzzles.");
           return;
       }


       System.out.println("Puzzles:");
       for (int i = 0; i < puzzles.size(); i++) {
           Puzzle puzzle = puzzles.get(i);
           System.out.printf("%2d) %s [%s] - %s%n",
                   i + 1,
                   puzzle.getName(),
                   puzzle.getType(),
                   puzzle.isSolved() ? "Solved" : "Unsolved");
       }
   }


   private static void attemptPuzzle(GameFacade game, Scanner scanner) {
       Optional<Room> currentRoom = game.getCurrentRoom();
       if (currentRoom.isEmpty()) {
           System.out.println("Enter a room before attempting a puzzle.");
           return;
       }


       List<Puzzle> puzzles = currentRoom.get().getPuzzles();
       if (puzzles.isEmpty()) {
           System.out.println("The current room does not contain any puzzles.");
           return;
       }


       showCurrentRoom(game);
       System.out.print("Choose a puzzle to attempt (leave blank to cancel): ");
       String input = scanner.nextLine().trim();
       if (input.isEmpty()) {
           return;
       }


       try {
           int index = Integer.parseInt(input) - 1;
           if (index < 0 || index >= puzzles.size()) {
               System.out.println("Puzzle selection out of range.");
               return;
           }


           Puzzle puzzle = puzzles.get(index);
           if (puzzle.isSolved()) {
               System.out.println("This puzzle is already solved.");
               return;
           }


           presentPuzzleDetails(puzzle);
           System.out.print("Enter your answer: ");
           String answer = scanner.nextLine();


           boolean solved = game.submitAnswer(puzzle.getId(), answer);
           if (solved) {
               System.out.println("Correct! Puzzle solved and progress updated.");
               if (!puzzle.getReward().isBlank()) {
                   System.out.println("Reward earned: " + puzzle.getReward());
               }
           } else {
               System.out.println("That is not the correct answer. Try again!");
           }
       } catch (NumberFormatException ex) {
           System.out.println("Invalid puzzle selection.");
       }
   }


   private static void presentPuzzleDetails(Puzzle puzzle) {
       System.out.println();
       System.out.println("Attempting: " + puzzle.getName());
       if (!puzzle.getDescription().isBlank()) {
           System.out.println("Description: " + puzzle.getDescription());
       }
       System.out.println("Type: " + puzzle.getType());


       if (puzzle instanceof MultipleChoicePuzzle mcPuzzle) {
           List<String> options = mcPuzzle.getOptions();
           if (!options.isEmpty()) {
               System.out.println("Options:");
               for (int i = 0; i < options.size(); i++) {
                   System.out.printf(" %d) %s%n", i + 1, options.get(i));
               }
           }
       } else if (puzzle instanceof SequencePuzzle sequencePuzzle) {
           System.out.println("Enter the sequence in order, separated by spaces or commas.");
           System.out.println("Expected entries: " + sequencePuzzle.getExpectedSequence().size());
       } else if (puzzle instanceof RiddlePuzzle riddlePuzzle) {
           System.out.println("Riddle: " + riddlePuzzle.getRiddle());
       } else if (puzzle instanceof CodeLockPuzzle) {
           System.out.println("Enter the code to unlock.");
       } else if (puzzle instanceof WriteInPuzzle) {
           System.out.println("Provide the correct answer.");
       }
       System.out.println();
   }


   private static void requestHint(GameFacade game) {
       Hint hint = game.getGameSystem().getHints().consumeNextHint();
       if (hint == null) {
           System.out.println("No hints remain.");
       } else {
           System.out.println("Hint: " + hint.getText());
       }
   }


   private static void showLeaderboard(GameFacade game) {
       List<ScoreEntry> scores = game.getLeaderboard().getScores();
       if (scores.isEmpty()) {
           System.out.println("Leaderboard is currently empty.");
           return;
       }


       System.out.println("Leaderboard:");
       for (int i = 0; i < scores.size(); i++) {
           ScoreEntry entry = scores.get(i);
           System.out.printf("%2d) %s - %d points (time: %s)%n",
                   i + 1,
                   entry.getPlayerName(),
                   entry.getScore(),
                   formatDuration(entry.getCompletionTime()));
       }
   }


   private static void saveGame(GameFacade game) {
       if (game.saveGame()) {
           System.out.println("Game state saved successfully.");
       } else {
           System.out.println("Failed to save the game state.");
       }
   }


   private static void reloadGame(GameFacade game, String dataDirectory) {
       if (game.loadGame()) {
           System.out.println("Reloaded game data from '" + dataDirectory + "'.");
       } else {
           System.out.println("Unable to reload game data from '" + dataDirectory + "'.");
       }
   }


   private static String formatRoomId(Room room) {
       if (room.getLegacyId() != null) {
           return "#" + room.getLegacyId();
       }
       return room.getId().toString();
   }


   private static String formatDuration(Duration duration) {
       Duration safeDuration = duration == null ? Duration.ZERO : duration;
       long seconds = safeDuration.getSeconds();
       long hours = seconds / 3600;
       long minutes = (seconds % 3600) / 60;
       long secs = seconds % 60;
       return String.format("%02d:%02d:%02d", hours, minutes, secs);
   }
}



