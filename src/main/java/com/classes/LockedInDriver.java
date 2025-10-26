package com.classes;

import com.lockedin.audio.PuzzleNarration;
import com.lockedin.audio.RoomNarration;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

/**
 * Entry point for the Locked-In escape room application.
 */
public class LockedInDriver {
    private static final String DEFAULT_DATA_DIR = "JSON";

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
        if (startingRoom.isEmpty()) {
            System.out.println("No rooms with unsolved puzzles were found. Please add rooms to the game data.");
            showEndScreen(game);
            return;
        }

        runEscapeExperience(game, scanner);

        showEndScreen(game);
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
    private static void runEscapeExperience(GameFacade game, Scanner scanner) {
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

                Optional<Puzzle> selection = promptPuzzleSelection(activeRoom, scanner);
                if (selection.isEmpty()) {
                    break;
                }

                boolean solved = attemptPuzzle(game, scanner, selection.get());
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
    private static boolean attemptPuzzle(GameFacade game, Scanner scanner, Puzzle puzzle) {
        System.out.println();
        presentPuzzleDetails(puzzle);

        while (true) {
            System.out.print("Enter your answer (press Enter to stop attempting this puzzle): ");
            String answer = scanner.nextLine();

            if (answer.trim().isEmpty()) {
                System.out.println("Leaving the puzzle unsolved for now.");
                return false;
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
            System.out.println("Player created with ID " + player.getId());
        } catch (IllegalArgumentException ex) {
            System.out.println("Unable to create player: " + ex.getMessage());
        }
    }

    /**
     * Displays a summary of the player's progress and highlights the leaderboard at the end of the session.
     *
     * @param game facade that provides final scores, timers, and leaderboard entries
     */
    private static void showEndScreen(GameFacade game) {
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

        System.out.println("Thanks for playing Locked-In!");
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
}
