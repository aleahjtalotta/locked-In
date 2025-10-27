package com.classes;

import com.lockedin.audio.PuzzleNarration;
import com.lockedin.audio.RoomNarration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/**
 * Entry point for the Locked-In escape room application.
 */
public class LockedInDriver {
    private static final String DEFAULT_DATA_DIR = "JSON";
    private static final String CERTIFICATE_DIRECTORY = "certificates";

    /**
     * Launches the Locked-In game using the provided data directory, or the default directory when none is supplied.
     *
     * @param args optional first argument that overrides the data directory root
     */
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

        System.out.println();
        System.out.println("--- Locked-In Escape Room ---");
        preparePlayer(game, scanner);

        Optional<Room> startingRoom = game.getCurrentRoom();
        SessionState sessionState = SessionState.from(game.getActivePlayer());
        if (startingRoom.isEmpty()) {
            System.out.println("No rooms with unsolved puzzles were found. Please add rooms to the game data.");
            showEndScreen(game, sessionState);
            return;
        }

        sessionState = runEscapeExperience(game, scanner, sessionState);

        showEndScreen(game, sessionState);
        if (!game.saveGame()) {
            System.out.println("Warning: progress could not be saved.");
        }
    }

    /**
     * Prompts the user to choose or create an active player profile before gameplay begins.
     *
     * @param game    facade coordinating game state and persistence
     * @param scanner console input source for user interactions
     */
    private static void preparePlayer(GameFacade game, Scanner scanner) {
        while (game.getActivePlayer().isEmpty()) {
            List<Player> players = game.getPlayerList().asList();
            if (players.isEmpty()) {
                System.out.println("No players found. Let's create a new profile before we begin.");
                createPlayer(game, scanner);
                continue;
            }

            System.out.println();
            System.out.println("Who is playing today?");
            listPlayers(game);
            System.out.println("Enter a player number, email, or type 'new' to add someone else.");
            System.out.print("Selection (press Enter to continue as guest): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Continuing without a logged-in player.");
                break;
            }

            if ("new".equalsIgnoreCase(input)) {
                createPlayer(game, scanner);
                continue;
            }

            boolean success = false;
            if (input.matches("\\d+")) {
                int index = Integer.parseInt(input) - 1;
                players = game.getPlayerList().asList();
                if (index >= 0 && index < players.size()) {
                    success = game.loginPlayer(players.get(index).getId());
                }
            } else {
                success = game.login(input);
            }

            if (success) {
                System.out.println("Welcome, " + game.getActivePlayer().map(Player::getName).orElse("player") + "!");
            } else {
                System.out.println("That selection did not match any player. Try again or press Enter to continue as guest.");
            }
        }
    }

    /**
     * Drives the main escape-room loop, presenting rooms and puzzles until the user stops or content ends.
     *
     * @param game    facade orchestrating the game and timer
     * @param scanner console input source for puzzle answers and decisions
     */
    private static SessionState runEscapeExperience(GameFacade game, Scanner scanner, SessionState sessionState) {
        game.startTimerCountdown();
        try {
            UUID lastRoomId = null;
            boolean continuePlaying = true;

            while (continuePlaying) {
                Optional<Room> activeRoomOpt = game.getCurrentRoom();
                if (activeRoomOpt.isEmpty()) {
                    System.out.println("There are no more puzzles left to solve.");
                    break;
                }

                Room activeRoom = activeRoomOpt.get();
                if (!activeRoom.getId().equals(lastRoomId)) {
                    System.out.println();
                    System.out.println("You enter room " + formatRoomId(activeRoom) + ".");
                    String narration = RoomNarration.createStory(activeRoom);
                    if (!narration.isBlank()) {
                        System.out.println(narration);
                    }
                    RoomNarration.narrateAsync(activeRoom);
                    lastRoomId = activeRoom.getId();
                }

                printRoomSummary(activeRoom);
                collectRoomItems(activeRoom, sessionState, game.getActivePlayer(), scanner);
                printInventory(sessionState);

                Optional<Puzzle> selection = promptPuzzleSelection(activeRoom, scanner);
                if (selection.isEmpty()) {
                    break;
                }

                boolean solved = attemptPuzzle(game, scanner, selection.get(), sessionState);
                if (!solved) {
                    continuePlaying = askYesNo(scanner, "Would you like to choose a different puzzle? (y/n): ");
                    continue;
                }

                if (game.getCurrentRoom().isEmpty()) {
                    System.out.println("You've completed every available puzzle!");
                    break;
                }

                continuePlaying = askYesNo(scanner, "Attempt another puzzle? (y/n): ");
            }
        } finally {
            game.pauseTimerCountdown();
        }
        return sessionState;
    }

    /**
     * Asks the player to select an unsolved puzzle within the active room.
     *
     * @param room    current room containing puzzles
     * @param scanner console input source for puzzle selection
     * @return selected puzzle wrapped in {@link Optional}, or empty if the player declines
     */
    private static Optional<Puzzle> promptPuzzleSelection(Room room, Scanner scanner) {
        while (true) {
            List<Puzzle> unsolved = room.getPuzzles().stream()
                    .filter(puzzle -> !puzzle.isSolved())
                    .toList();

            if (unsolved.isEmpty()) {
                System.out.println("All puzzles in this room are already solved.");
                return Optional.empty();
            }

            System.out.println();
            System.out.println("Choose a puzzle to attempt:");
            for (int i = 0; i < unsolved.size(); i++) {
                Puzzle puzzle = unsolved.get(i);
                System.out.printf("%2d) %s [%s]%n", i + 1, puzzle.getName(), puzzle.getType());
            }
            System.out.print("Enter puzzle number (press Enter to finish): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return Optional.empty();
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < unsolved.size()) {
                    return Optional.of(unsolved.get(index));
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Invalid selection. Try again.");
        }
    }

    /**
     * Handles repeated answer submissions for the supplied puzzle until it is solved or the player quits.
     *
     * @param game    facade granting access to puzzle evaluation and rewards
     * @param scanner console input source for player answers
     * @param puzzle  puzzle being attempted
     * @return {@code true} when the puzzle is solved; {@code false} if the player exits early
     */
    private static boolean attemptPuzzle(GameFacade game, Scanner scanner, Puzzle puzzle, SessionState sessionState) {
        System.out.println();
        presentPuzzleDetails(puzzle);

        String suggestedItem = recommendItemFor(puzzle);
        if (suggestedItem != null) {
            if (sessionState.hasItemNamed(suggestedItem)) {
                System.out.println("The " + suggestedItem + " vibrates softly. Type 'item' if you want to use it for extra insight.");
            } else {
                System.out.println("You sense this challenge reacts to the " + suggestedItem + ". Search the manor if you have not picked it up yet.");
            }
        }

        while (true) {
            System.out.print("Enter your answer (type 'hint' or 'item', Enter to stop): ");
            String answer = scanner.nextLine().trim();

            if (answer.isEmpty()) {
                System.out.println("Leaving the puzzle unsolved for now.");
                return false;
            }

            if ("hint".equalsIgnoreCase(answer)) {
                if (!revealHint(game, sessionState)) {
                    System.out.println("No hints remain for this session.");
                }
                continue;
            }

            if ("item".equalsIgnoreCase(answer) || "use".equalsIgnoreCase(answer)) {
                if (!useItemOnPuzzle(scanner, puzzle, sessionState)) {
                    System.out.println("No usable items were selected.");
                }
                continue;
            }

            if (puzzle instanceof MultipleChoicePuzzle multipleChoicePuzzle) {
                Optional<String> resolved = resolveMultipleChoiceAnswer(answer, multipleChoicePuzzle);
                if (resolved.isEmpty()) {
                    System.out.println("Please enter a valid choice number or answer text shown in the list.");
                    continue;
                }
                answer = resolved.get();
            }

            boolean solved = game.submitAnswer(puzzle.getId(), answer);
            if (solved) {
                System.out.println("Correct! Puzzle solved and progress updated.");
                if (!puzzle.getReward().isBlank()) {
                    System.out.println("Reward earned: " + puzzle.getReward());
                }
                return true;
            }

            System.out.println("That is not the correct answer.");
            if (!askYesNo(scanner, "Try again? (y/n): ")) {
                return false;
            }
        }
    }

    /**
     * Outputs key information and narration cues needed for the player to understand the puzzle.
     *
     * @param puzzle puzzle whose description, type, and narration should be shown
     */
    private static void presentPuzzleDetails(Puzzle puzzle) {
        System.out.println();
        System.out.println("Attempting: " + puzzle.getName());
        if (!puzzle.getDescription().isBlank()) {
            System.out.println("Description: " + puzzle.getDescription());
        }
        System.out.println("Type: " + puzzle.getType());
        System.out.println("(You can type 'hint' for a clue or 'item' to use something you've collected.)");

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
        PuzzleNarration.narrateAsync(puzzle);
    }

    /**
     * Displays an overview of the room, including items and puzzle completion status.
     *
     * @param room room currently explored by the player
     */
    private static void printRoomSummary(Room room) {
        System.out.println("Room details:");
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

    /**
     * Prompts the user for a yes or no response and keeps asking until a definitive answer is provided.
     *
     * @param scanner console input source
     * @param prompt  question to display to the user
     * @return {@code true} for affirmative responses, {@code false} for negative or empty responses
     */
    private static boolean askYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.isEmpty()) {
                return false;
            }

            if (response.startsWith("y")) {
                return true;
            }

            if (response.startsWith("n")) {
                return false;
            }

            System.out.println("Please answer with 'y' or 'n'.");
        }
    }

    /**
     * Prints all registered players so the user can choose an existing profile.
     *
     * @param game facade providing access to the known players
     */
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

    /**
     * Collects details from the console to create a new player profile in the persistence store.
     *
     * @param game    facade used to store the newly created player
     * @param scanner console input source for name, email, and avatar path
     */
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
            game.login(player.getEmail());
            System.out.println("Player created with ID " + player.getId() + " and logged in.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Unable to create player: " + ex.getMessage());
        }
    }

    /**
     * Displays a summary of the player's progress and highlights the leaderboard at the end of the session.
     *
     * @param game facade that provides final scores, timers, and leaderboard entries
     */
    private static void showEndScreen(GameFacade game, SessionState sessionState) {
        System.out.println();
        System.out.println("=== Escape Summary ===");
        game.getActivePlayer()
                .ifPresentOrElse(
                        player -> System.out.println("Player: " + player.getName() + " | Score: " + player.getCurrentScore()),
                        () -> System.out.println("Player: Guest session"));

        int totalPuzzles = game.getRooms().stream()
                .mapToInt(room -> room.getPuzzles().size())
                .sum();
        int solvedPuzzles = game.getGameSystem().getProgress().getSolvedPuzzleIds().size();
        System.out.printf("Puzzles solved: %d/%d%n", solvedPuzzles, totalPuzzles);

        Timer timer = game.getGameSystem().getTimer();
        if (timer != null && (!timer.getTotalTime().isZero() || !timer.getRemaining().isZero())) {
            System.out.println("Timer total: " + formatDuration(timer.getTotalTime()));
            System.out.println("Time remaining: " + formatDuration(timer.getRemaining()));
        }
        if (timer != null) {
            System.out.println("Time elapsed: " + formatDuration(timer.getElapsed()));
        }

        if (!game.getLeaderboard().getScores().isEmpty()) {
            System.out.println();
            showLeaderboard(game);
        }

        int hintsUsed = 0;
        if (sessionState != null) {
            hintsUsed = sessionState.getHintsUsed();
            System.out.println();
            System.out.println("Items collected this run: " + sessionState.describeCollectedItems());
            System.out.println("Hints used: " + hintsUsed);
        }

        if (totalPuzzles >= 3 && solvedPuzzles == totalPuzzles) {
            generateCompletionCertificate(game, sessionState, solvedPuzzles, totalPuzzles, hintsUsed)
                    .ifPresent(path -> System.out.println("Completion certificate saved to: " + path.toAbsolutePath()));
        }

        System.out.println("Thanks for playing Locked-In!");
    }

    private static Optional<Path> generateCompletionCertificate(GameFacade game,
                                                                SessionState sessionState,
                                                                int solvedPuzzles,
                                                                int totalPuzzles,
                                                                int hintsUsed) {
        Timer timer = game.getGameSystem().getTimer();
        Duration elapsed = timer == null ? Duration.ZERO : timer.getElapsed();
        String elapsedFormatted = formatDuration(elapsed);

        String playerName = game.getActivePlayer()
                .map(Player::getName)
                .orElse("Guest");
        int finalScore = game.getActivePlayer()
                .map(Player::getCurrentScore)
                .orElse(Math.max(0, solvedPuzzles * 5 - hintsUsed));

        DifficultyLevel difficulty = game.getGameSystem().getDifficulty();
        String difficultyLabel = formatDifficultyLabel(difficulty);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fileStampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss");
        String sanitizedName = playerName.replaceAll("[^a-zA-Z0-9]+", "_").toLowerCase();
        if (sanitizedName.isBlank()) {
            sanitizedName = "guest";
        }
        String fileName = String.format("LockedIn_Certificate_%s_%s.txt", sanitizedName, now.format(fileStampFormatter));
        Path certificateDir = Path.of(CERTIFICATE_DIRECTORY);
        String newline = System.lineSeparator();

        StringBuilder content = new StringBuilder()
                .append("========================================").append(newline)
                .append("          Locked In - Certificate       ").append(newline)
                .append("========================================").append(newline).append(newline)
                .append("Congrats!! You Locked-In and escaped!").append(newline).append(newline)
                .append("Game: Locked In").append(newline)
                .append("Player: ").append(playerName).append(newline)
                .append("Difficulty: ").append(difficultyLabel).append(newline)
                .append("Time Taken: ").append(elapsedFormatted).append(newline)
                .append("Hints Used: ").append(hintsUsed).append(newline)
                .append("Puzzles Solved: ").append(solvedPuzzles).append("/").append(totalPuzzles).append(newline)
                .append("Final Score: ").append(finalScore).append(newline)
                .append("Completed On: ").append(now.format(displayFormatter)).append(newline);

        try {
            Files.createDirectories(certificateDir);
            Path certificatePath = certificateDir.resolve(fileName);
            Files.writeString(certificatePath, content.toString());
            return Optional.of(certificatePath);
        } catch (IOException ex) {
            System.out.println("Unable to create completion certificate: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private static String formatDifficultyLabel(DifficultyLevel difficulty) {
        if (difficulty == null) {
            return "Unknown";
        }
        String name = difficulty.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Lets players take the available items from the current room so they can satisfy
     * the haunted-mansion scenario requirements.
     */
    private static void collectRoomItems(Room room, SessionState sessionState, Optional<Player> activePlayer, Scanner scanner) {
        List<Item> items = room.getItems();
        List<Item> newItems = items.stream()
                .filter(item -> !sessionState.hasItem(item))
                .toList();
        if (newItems.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("You spot a few curiosities lying around:");
        for (Item item : newItems) {
            boolean takeItem = askYesNo(scanner, "Pick up the " + item.getName() + "? (y/n): ");
            if (!takeItem) {
                continue;
            }
            sessionState.addItem(item);
            activePlayer.ifPresent(player -> addItemToPlayer(player, item));
            System.out.println("You stash the " + item.getName() + " in your bag." + (item.isReusable() ? " It feels sturdy enough to reuse." : " It might crumble after one use."));
        }
        System.out.println();
    }

    private static void printInventory(SessionState sessionState) {
        if (sessionState.getInventory().isEmpty()) {
            System.out.println("Inventory: (no items collected yet)");
            return;
        }
        System.out.println("Inventory:");
        List<Item> inventory = sessionState.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            System.out.printf(" %d) %s%s%n", i + 1, item.getName(), sessionState.wasItemUsed(item) ? " (used)" : "");
        }
    }

    private static boolean useItemOnPuzzle(Scanner scanner, Puzzle puzzle, SessionState sessionState) {
        if (sessionState.getInventory().isEmpty()) {
            System.out.println("Your bag is empty. Explore another room for tools.");
            return false;
        }
        System.out.println("Choose an item to use (press Enter to cancel):");
        List<Item> inventory = sessionState.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            System.out.printf(" %d) %s%s%n", i + 1, item.getName(), item.isReusable() ? " (reusable)" : " (fragile)" );
        }
        System.out.print("Selection: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return false;
        }
        try {
            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= inventory.size()) {
                return false;
            }
            Item item = inventory.get(index);
            sessionState.markItemUsed(item);
            System.out.println(describeItemEffect(item, puzzle));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static String describeItemEffect(Item item, Puzzle puzzle) {
        return switch (item.getName()) {
            case "Shadow Candle" -> "The candlelight bends, revealing ghostly letters that make the answer clearer.";
            case "Lantern Dial" -> "You twist the dial and replay the blinking pattern at will.";
            case "Frame Hook" -> "The hook lets you lift every portrait safely, making swaps painless.";
            case "Mirror Brush" -> "Dust vanishes from the mirror, showing which reflection still moves.";
            case "Ash Brush" -> "Soot sweeps aside to expose the etched numbers.";
            case "Chant Scroll" -> "Reciting from the scroll makes the final echoed word ring louder.";
            default -> "Using the " + item.getName() + " helps you focus on " + puzzle.getName() + ".";
        };
    }

    private static String recommendItemFor(Puzzle puzzle) {
        Long legacyId = puzzle.getLegacyId();
        if (legacyId == null) {
            return null;
        }
        return switch (legacyId.intValue()) {
            case 301 -> "Shadow Candle";
            case 302 -> "Lantern Dial";
            case 303 -> "Frame Hook";
            case 304 -> "Mirror Brush";
            case 305 -> "Ash Brush";
            case 306 -> "Chant Scroll";
            default -> null;
        };
    }

    /**
     * Resolves user input for a multiple-choice puzzle into the canonical option string so answer validation
     * succeeds when the player provides a number, letter, or partial text.
     *
     * @param input  raw player input gathered from the console
     * @param puzzle the multiple choice puzzle being attempted
     * @return optional containing the normalized option text when the input maps to a known option
     */
    private static Optional<String> resolveMultipleChoiceAnswer(String input, MultipleChoicePuzzle puzzle) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        List<String> options = puzzle.getOptions();

        if (trimmed.matches("\\d+")) {
            int index = Integer.parseInt(trimmed) - 1;
            if (index >= 0 && index < options.size()) {
                return Optional.of(options.get(index));
            }
            return Optional.empty();
        }

        if (trimmed.length() == 1 && Character.isLetter(trimmed.charAt(0))) {
            int index = Character.toUpperCase(trimmed.charAt(0)) - 'A';
            if (index >= 0 && index < options.size()) {
                return Optional.of(options.get(index));
            }
        }

        for (String option : options) {
            if (option.equalsIgnoreCase(trimmed)) {
                return Optional.of(option);
            }
        }

        String lower = trimmed.toLowerCase();
        List<String> matches = options.stream()
                .filter(option -> option.toLowerCase().contains(lower))
                .toList();
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }
        return Optional.empty();
    }

    private static boolean revealHint(GameFacade game, SessionState sessionState) {
        Optional<Hint> hint = game.useHint();
        if (hint.isEmpty()) {
            return false;
        }
        sessionState.recordHintUse();
        System.out.println("Hint: " + hint.get().getText());
        return true;
    }

    private static void addItemToPlayer(Player player, Item item) {
        boolean alreadyOwned = player.getInventory().asList().stream()
                .anyMatch(existing -> existing.getId().equals(item.getId()));
        if (!alreadyOwned) {
            player.getInventory().add(item);
        }
    }

    /**
     * Prints the leaderboard in order so players can compare high scores.
     *
     * @param game facade that exposes the leaderboard entries
     */
    private static void showLeaderboard(GameFacade game) {
        List<ScoreEntry> scores = game.getLeaderboard().getScores();
        if (scores.isEmpty()) {
            System.out.println("Leaderboard is currently empty.");
            return;
        }

        System.out.println("Leaderboard:");
        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            System.out.printf("%2d) %s - %d points%n",
                    i + 1,
                    entry.getPlayerName(),
                    entry.getScore());
        }
    }

    /**
     * Formats a room identifier, preferring the legacy numeric reference when available.
     *
     * @param room room whose identifier should be displayed
     * @return formatted identifier string for console output
     */
    private static String formatRoomId(Room room) {
        if (room.getLegacyId() != null) {
            return "#" + room.getLegacyId();
        }
        return room.getId().toString();
    }

    /**
     * Converts a {@link Duration} to an {@code HH:MM:SS} string, treating {@code null} values as zero.
     *
     * @param duration duration to format
     * @return formatted time string safe for user display
     */
    private static String formatDuration(Duration duration) {
        Duration safeDuration = duration == null ? Duration.ZERO : duration;
        long seconds = safeDuration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private static final class SessionState {
        private final List<Item> inventory;
        private final Set<UUID> usedItemIds;
        private final Set<String> collectedNames;
        private int hintsUsed;

        private SessionState(List<Item> seed) {
            this.inventory = new ArrayList<>(seed);
            this.usedItemIds = new HashSet<>();
            this.collectedNames = new LinkedHashSet<>();
            seed.forEach(item -> collectedNames.add(item.getName()));
        }

        static SessionState from(Optional<Player> player) {
            List<Item> seed = new ArrayList<>();
            player.ifPresent(value -> seed.addAll(value.getInventory().asList()));
            return new SessionState(seed);
        }

        List<Item> getInventory() {
            return List.copyOf(inventory);
        }

        boolean hasItem(Item item) {
            return inventory.stream().anyMatch(existing -> existing.getId().equals(item.getId()));
        }

        boolean hasItemNamed(String name) {
            return inventory.stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(name));
        }

        boolean wasItemUsed(Item item) {
            return usedItemIds.contains(item.getId());
        }

        void addItem(Item item) {
            if (!hasItem(item)) {
                inventory.add(item);
            }
            collectedNames.add(item.getName());
        }

        void markItemUsed(Item item) {
            usedItemIds.add(item.getId());
            if (!item.isReusable()) {
                inventory.removeIf(existing -> existing.getId().equals(item.getId()));
            }
        }

        void recordHintUse() {
            hintsUsed++;
        }

        int getHintsUsed() {
            return hintsUsed;
        }

        String describeCollectedItems() {
            if (collectedNames.isEmpty()) {
                return "none";
            }
            return String.join(", ", collectedNames);
        }
    }
}
