package com.lockedin.ui;

import com.classes.CodeLockPuzzle;
import com.classes.GameFacade;
import com.classes.Hint;
import com.classes.Leaderboard;
import com.classes.MultipleChoicePuzzle;
import com.classes.Player;
import com.classes.Puzzle;
import com.classes.RiddlePuzzle;
import com.classes.Room;
import com.classes.ScoreEntry;
import com.classes.SequencePuzzle;
import com.classes.Timer;
import com.classes.WriteInPuzzle;
import com.lockedin.audio.PuzzleNarration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple JavaFX front-end for the Locked-In escape room.
 */
public class LockedInApp extends Application {
    private static final String DEFAULT_DATA_DIR = "JSON";

    private GameFacade game;
    private String dataDirectory;

    private final ObservableList<Player> players = FXCollections.observableArrayList();
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Puzzle> puzzles = FXCollections.observableArrayList();
    private final ObservableList<String> leaderboardEntries = FXCollections.observableArrayList();

    private ListView<Player> playersView;
    private ListView<Room> roomsView;
    private ListView<Puzzle> puzzlesView;
    private ListView<String> leaderboardView;

    private final Label activePlayerLabel = new Label("Active player: none");
    private final Label timerLabel = new Label("Timer: --:--:-- remaining");
    private final Label hintsLabel = new Label("Hints remaining: 0");
    private final Label dataDirectoryLabel = new Label();
    private final TextArea puzzleDetailsArea = new TextArea();

    private Timeline timerTimeline;
    private UUID lastNarratedPuzzleId;

    @Override
    public void init() {
        List<String> args = getParameters().getRaw();
        this.dataDirectory = args.isEmpty() ? DEFAULT_DATA_DIR : args.get(0);
        this.game = new GameFacade(dataDirectory);
        if (!game.loadGame()) {
            game.startNewGame();
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Locked-In Escape Room");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));

        root.setTop(buildTopBar());
        root.setLeft(buildPlayersPane());
        root.setCenter(buildRoomsPane());
        root.setRight(buildPuzzlesPane());
        root.setBottom(buildLeaderboardPane());

        Scene scene = new Scene(root, 1200, 680);
        stage.setScene(scene);
        stage.show();

        refreshGameState();
        startTimerUpdates();
    }

    @Override
    public void stop() {
        if (timerTimeline != null) {
            timerTimeline.stop();
        }
    }

    private VBox buildTopBar() {
        dataDirectoryLabel.setText("Data directory: " + dataDirectory);

        Label title = new Label("Locked-In Escape Room");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveGame());

        Button reloadButton = new Button("Reload");
        reloadButton.setOnAction(e -> reloadGame());

        Button hintButton = new Button("Use Hint");
        hintButton.setOnAction(e -> consumeHint());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(12, saveButton, reloadButton, hintButton, spacer, exitButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox top = new VBox(8, title, dataDirectoryLabel, controls, activePlayerLabel, timerLabel, hintsLabel);
        top.setPadding(new Insets(0, 0, 16, 0));
        return top;
    }

    private VBox buildPlayersPane() {
        Label header = new Label("Players");
        header.setStyle("-fx-font-weight: bold;");

        playersView = new ListView<>(players);
        playersView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty || player == null) {
                    setText(null);
                } else {
                    setText(player.getName() + " — score: " + player.getCurrentScore());
                }
            }
        });
        playersView.getSelectionModel().selectedItemProperty().addListener((obs, oldPlayer, newPlayer) -> {
            if (newPlayer != null) {
                game.loginPlayer(newPlayer.getId());
                updateActivePlayerLabel();
            }
        });

        Button createButton = new Button("Create Player");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(e -> promptNewPlayer());

        Button logoutButton = new Button("Log Out");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(e -> {
            playersView.getSelectionModel().clearSelection();
            game.logoutPlayer();
            updateActivePlayerLabel();
        });

        VBox box = new VBox(8, header, playersView, createButton, logoutButton);
        box.setPadding(new Insets(0, 16, 0, 0));
        box.setPrefWidth(240);
        VBox.setVgrow(playersView, Priority.ALWAYS);
        return box;
    }

    private VBox buildRoomsPane() {
        Label header = new Label("Rooms");
        header.setStyle("-fx-font-weight: bold;");

        roomsView = new ListView<>(rooms);
        roomsView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                } else {
                    long solved = room.getPuzzles().stream().filter(Puzzle::isSolved).count();
                    setText(formatRoomId(room) + " — solved " + solved + "/" + room.getPuzzles().size());
                }
            }
        });
        roomsView.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
            if (newRoom != null) {
                game.enterRoom(newRoom.getId());
                refreshPuzzles(newRoom);
            } else {
                puzzles.clear();
                puzzlesView.getSelectionModel().clearSelection();
            }
        });

        VBox box = new VBox(8, header, roomsView);
        VBox.setVgrow(roomsView, Priority.ALWAYS);
        return box;
    }

    private VBox buildPuzzlesPane() {
        Label header = new Label("Puzzles");
        header.setStyle("-fx-font-weight: bold;");

        puzzlesView = new ListView<>(puzzles);
        puzzlesView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Puzzle puzzle, boolean empty) {
                super.updateItem(puzzle, empty);
                if (empty || puzzle == null) {
                    setText(null);
                } else {
                    setText(puzzle.getName() + (puzzle.isSolved() ? " (Solved)" : ""));
                }
            }
        });
        puzzlesView.getSelectionModel().selectedItemProperty().addListener((obs, oldPuzzle, newPuzzle) -> {
            showPuzzleDetails(newPuzzle);
        });

        puzzleDetailsArea.setEditable(false);
        puzzleDetailsArea.setWrapText(true);
        puzzleDetailsArea.setPrefRowCount(12);

        Button storyButton = new Button("Play Story");
        storyButton.setMaxWidth(Double.MAX_VALUE);
        storyButton.disableProperty().bind(puzzlesView.getSelectionModel().selectedItemProperty().isNull());
        storyButton.setOnAction(e -> {
            Puzzle puzzle = puzzlesView.getSelectionModel().getSelectedItem();
            narratePuzzle(puzzle, true);
        });

        Button attemptButton = new Button("Attempt Puzzle");
        attemptButton.setMaxWidth(Double.MAX_VALUE);
        attemptButton.disableProperty().bind(puzzlesView.getSelectionModel().selectedItemProperty().isNull());
        attemptButton.setOnAction(e -> attemptSelectedPuzzle());

        VBox box = new VBox(8, header, puzzlesView, puzzleDetailsArea, storyButton, attemptButton);
        box.setPadding(new Insets(0, 0, 0, 16));
        box.setPrefWidth(360);
        VBox.setVgrow(puzzlesView, Priority.ALWAYS);
        return box;
    }

    private VBox buildLeaderboardPane() {
        Label header = new Label("Leaderboard");
        header.setStyle("-fx-font-weight: bold;");

        leaderboardView = new ListView<>(leaderboardEntries);
        leaderboardView.setFocusTraversable(false);

        VBox box = new VBox(8, header, leaderboardView);
        box.setPadding(new Insets(16, 0, 0, 0));
        return box;
    }

    private void promptNewPlayer() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Create Player");
        nameDialog.setHeaderText("Enter the player's display name.");
        nameDialog.setContentText("Name:");
        Optional<String> nameResult = nameDialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty());
        if (nameResult.isEmpty()) {
            return;
        }

        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Create Player");
        emailDialog.setHeaderText("Enter the player's email address.");
        emailDialog.setContentText("Email:");
        Optional<String> emailResult = emailDialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty());
        if (emailResult.isEmpty()) {
            return;
        }

        try {
            Player player = game.createAccount(nameResult.get(), emailResult.get(), null);
            refreshGameState();
            selectPlayerById(player.getId());
            showInformation("Player Created", "Added " + player.getName() + " to the roster.");
        } catch (IllegalArgumentException ex) {
            showError("Unable to create player", ex.getMessage());
        }
    }

    private void attemptSelectedPuzzle() {
        Puzzle puzzle = puzzlesView.getSelectionModel().getSelectedItem();
        if (puzzle == null) {
            return;
        }

        if (puzzle.isSolved()) {
            showInformation("Already Solved", "This puzzle has already been solved.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Attempt Puzzle");
        dialog.setHeaderText(puzzle.getName());
        dialog.setContentText(buildPromptForPuzzle(puzzle));

        Optional<String> result = dialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty());
        if (result.isEmpty()) {
            return;
        }

        boolean solved = game.submitAnswer(puzzle.getId(), result.get());
        if (solved) {
            showInformation("Success", "Correct answer! Puzzle solved.");
        } else {
            showError("Incorrect", "That answer did not solve the puzzle. Try again!");
        }

        refreshAfterPuzzleAttempt();
    }

    private void refreshAfterPuzzleAttempt() {
        Room currentRoom = game.getCurrentRoom().orElse(null);
        if (currentRoom != null) {
            refreshPuzzles(currentRoom);
            selectRoomById(currentRoom.getId());
        } else {
            puzzles.clear();
        }
        puzzlesView.refresh();
        roomsView.refresh();
        playersView.refresh();
        showPuzzleDetails(puzzlesView.getSelectionModel().getSelectedItem());
        refreshLeaderboard();
        updateActivePlayerLabel();
        updateTimerLabel();
    }

    private String buildPromptForPuzzle(Puzzle puzzle) {
        if (puzzle instanceof MultipleChoicePuzzle mc) {
            StringBuilder prompt = new StringBuilder("Choose the correct option:\n");
            List<String> options = mc.getOptions();
            for (int i = 0; i < options.size(); i++) {
                prompt.append(i + 1).append(") ").append(options.get(i)).append("\n");
            }
            prompt.append("\nType the matching option text.");
            return prompt.toString();
        } else if (puzzle instanceof SequencePuzzle sequence) {
            return "Enter the sequence in order, separated by commas.\nExpected entries: " + sequence.getExpectedSequence().size();
        } else if (puzzle instanceof RiddlePuzzle) {
            return "Type your answer to the riddle.";
        } else if (puzzle instanceof CodeLockPuzzle) {
            return "Enter the code to unlock the mechanism.";
        } else if (puzzle instanceof WriteInPuzzle) {
            return "Enter the answer.";
        }
        return "Provide your answer.";
    }

    private void showPuzzleDetails(Puzzle puzzle) {
        if (puzzle == null) {
            puzzleDetailsArea.clear();
            lastNarratedPuzzleId = null;
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(puzzle.getName()).append("\n");
        details.append("Type: ").append(puzzle.getType()).append("\n");
        details.append("Status: ").append(puzzle.isSolved() ? "Solved" : "Unsolved").append("\n");
        if (!puzzle.getReward().isBlank()) {
            details.append("Reward: ").append(puzzle.getReward()).append("\n");
        }
        details.append("\n").append(puzzle.getDescription()).append("\n\n");

        if (puzzle instanceof MultipleChoicePuzzle mc) {
            details.append("Options:\n");
            List<String> options = mc.getOptions();
            for (int i = 0; i < options.size(); i++) {
                details.append(" • ").append(options.get(i)).append("\n");
            }
        } else if (puzzle instanceof SequencePuzzle sequence) {
            details.append("Sequence length: ").append(sequence.getExpectedSequence().size()).append("\n");
            details.append("Enter each step separated by commas.");
        } else if (puzzle instanceof RiddlePuzzle riddle) {
            details.append("Riddle:\n").append(riddle.getRiddle()).append("\n");
        } else if (puzzle instanceof CodeLockPuzzle) {
            details.append("This is a code lock. Enter the correct code to solve it.");
        } else if (puzzle instanceof WriteInPuzzle) {
            details.append("Provide the correct word or phrase to solve.");
        }

        puzzleDetailsArea.setText(details.toString());
        narratePuzzle(puzzle, false);
    }

    private void consumeHint() {
        Optional<Hint> hint = game.useHint();
        if (hint.isEmpty()) {
            showInformation("No Hints Left", "You have used every available hint.");
            return;
        }
        game.saveGame();
        showInformation("Hint", hint.get().getText());
        refreshHintsLabel();
        refreshLeaderboard();
        playersView.refresh();
        updateActivePlayerLabel();
    }

    private void saveGame() {
        if (game.saveGame()) {
            showInformation("Saved", "Game state saved to " + dataDirectory + ".");
        } else {
            showError("Save Failed", "Unable to save the current game state.");
        }
    }

    private void reloadGame() {
        if (game.loadGame()) {
            refreshGameState();
            showInformation("Reloaded", "Game data reloaded from " + dataDirectory + ".");
        } else {
            showError("Reload Failed", "Unable to load data from " + dataDirectory + ".");
        }
    }

    private void refreshGameState() {
        players.setAll(game.getPlayerList().asList());
        rooms.setAll(game.getRooms());
        refreshLeaderboard();
        refreshHintsLabel();
        updateActivePlayerLabel();
        updateTimerLabel();

        if (!rooms.isEmpty()) {
            Room current = game.getCurrentRoom().orElse(rooms.get(0));
            selectRoomById(current.getId());
        } else {
            puzzles.clear();
            puzzleDetailsArea.clear();
        }

        game.getActivePlayer().ifPresent(player -> selectPlayerById(player.getId()));
    }

    private void refreshPuzzles(Room room) {
        puzzles.setAll(room.getPuzzles());
        puzzlesView.getSelectionModel().clearSelection();
        puzzleDetailsArea.clear();
    }

    private void refreshLeaderboard() {
        Leaderboard leaderboard = game.getLeaderboard();
        leaderboardEntries.setAll(leaderboard.getScores().stream()
                .map(this::formatScoreEntry)
                .toList());
    }

    private void refreshHintsLabel() {
        int remaining = game.getGameSystem().getHints().getRemainingHints().size();
        hintsLabel.setText("Hints remaining: " + remaining);
    }

    private void updateActivePlayerLabel() {
        game.getActivePlayer().ifPresentOrElse(
                player -> activePlayerLabel.setText("Active player: " + player.getName() + " — score: " + player.getCurrentScore()),
                () -> activePlayerLabel.setText("Active player: none")
        );
    }

    private void updateTimerLabel() {
        Timer timer = game.getGameSystem().getTimer();
        if (timer == null) {
            timerLabel.setText("Timer: --:--:-- remaining");
            return;
        }
        String remaining = formatDuration(timer.getRemaining());
        String total = formatDuration(timer.getTotalTime());
        timerLabel.setText("Timer: " + remaining + " remaining of " + total);
    }

    private void startTimerUpdates() {
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimerLabel()));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private String formatRoomId(Room room) {
        if (room.getLegacyId() != null) {
            return "Room #" + room.getLegacyId();
        }
        return "Room " + room.getId();
    }

    private String formatScoreEntry(ScoreEntry entry) {
        return entry.getPlayerName() + " — " + entry.getScore() + " pts (time: " + formatDuration(entry.getCompletionTime()) + ")";
    }

    private String formatDuration(java.time.Duration duration) {
        java.time.Duration safe = (duration == null || duration.isNegative()) ? java.time.Duration.ZERO : duration;
        long seconds = safe.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return "%02d:%02d:%02d".formatted(hours, minutes, secs);
    }

    private void selectPlayerById(UUID id) {
        if (playersView == null) {
            return;
        }
        for (Player player : playersView.getItems()) {
            if (player.getId().equals(id)) {
                playersView.getSelectionModel().select(player);
                playersView.scrollTo(player);
                break;
            }
        }
    }

    private void selectRoomById(UUID id) {
        if (roomsView == null) {
            return;
        }
        for (Room room : roomsView.getItems()) {
            if (room.getId().equals(id)) {
                roomsView.getSelectionModel().select(room);
                roomsView.scrollTo(room);
                refreshPuzzles(room);
                lastNarratedPuzzleId = null;
                break;
            }
        }
    }

    private void showInformation(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private void narratePuzzle(Puzzle puzzle, boolean force) {
        if (puzzle == null) {
            return;
        }
        if (!force && puzzle.getId().equals(lastNarratedPuzzleId)) {
            return;
        }
        PuzzleNarration.narrateAsync(puzzle);
        lastNarratedPuzzleId = puzzle.getId();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
