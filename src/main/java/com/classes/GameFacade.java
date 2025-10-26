package com.classes;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Coordinates the escape-room gameplay loop. This facade hides the underlying
 * persistence and domain objects by loading/saving state, managing player
 * sessions, tracking progress, pacing rooms sequentially, and mediating score,
 * hint, and timer updates exposed to the UI layer.
 */
public class GameFacade {
    private static final int POINTS_PER_PUZZLE = 5;
    private static final int HINT_PENALTY = 1;
    private static final int SEQUENTIAL_ROOM_LIMIT = 3;
    private static final Duration DEFAULT_TIMER_DURATION = Duration.ofMinutes(15);
    private GameSystem gameSystem;
    private final DataLoader dataLoader;
    private final DataWriter dataWriter;
    private Player activePlayer;
    private boolean timerStarted;

    /**
     * Creates a facade that will load and save game data within the provided
     * directory while starting with a fresh in-memory {@link GameSystem}.
     *
     * @param dataDirectory base path containing {@code rooms.json} and {@code users.json}; must not be {@code null}
     */
    public GameFacade(String dataDirectory) {
        Objects.requireNonNull(dataDirectory, "dataDirectory");
        Path basePath = Path.of(dataDirectory);
        this.dataLoader = new DataLoader(basePath);
        this.dataWriter = new DataWriter(basePath);
        this.gameSystem = new GameSystem();
        this.timerStarted = false;
    }

    /**
     * Reinitializes the game system to a clean state, clearing solved puzzles,
     * resetting the timer, and discarding any active player session.
     */
    public void startNewGame() {
        this.gameSystem = new GameSystem();
        this.activePlayer = null;
        this.gameSystem.getProgress().clearSolved();
        clearAllPuzzleSolvedFlags();
        ensureCurrentRoom();
        timerStarted = false;
        configureSessionTimer();
    }

    /**
     * Loads the game state from disk and prepares the facade for interaction.
     *
     * @return {@code true} when the data was loaded successfully; {@code false} otherwise
     */
    public boolean loadGame() {
        return dataLoader.loadGame().map(loaded -> {
            this.gameSystem = loaded;
            this.activePlayer = null;
            this.gameSystem.getProgress().clearSolved();
            clearAllPuzzleSolvedFlags();
            ensureCurrentRoom();
            timerStarted = false;
            configureSessionTimer();
            return true;
        }).orElse(false);
    }

    /**
     * Saves the in-memory game system (including the active player's progress)
     * back to disk.
     *
     * @return {@code true} when the game state was written successfully; {@code false} on failure
     */
    public boolean saveGame() {
        persistActiveProgress();
        return dataWriter.saveGame(gameSystem);
    }

    /**
     * Logs in a player by identifier.
     *
     * @param playerId unique identifier of the player to activate
     * @return {@code true} if the player exists and is now active; {@code false} otherwise
     */
    public boolean loginPlayer(UUID playerId) {
        Optional<Player> player = gameSystem.getPlayers().findById(playerId);
        player.ifPresent(this::setActivePlayer);
        return player.isPresent();
    }

    /**
     * Logs in a player by email address.
     *
     * @param email email used to locate the player
     * @return {@code true} if the player exists and is now active; {@code false} otherwise
     */
    public boolean login(String email) {
        Optional<Player> player = gameSystem.getPlayers().findByEmail(email);
        player.ifPresent(this::setActivePlayer);
        return player.isPresent();
    }

    /**
     * Creates a new player record and persists the updated roster.
     *
     * @param name   display name for the player
     * @param email  login email for the player
     * @param avatar avatar identifier or path
     * @return the newly created {@link Player}
     */
    public Player createAccount(String name, String email, String avatar) {
        Player player = gameSystem.getPlayers().createPlayer(name, email, avatar);
        saveGame();
        return player;
    }

    /**
     * Checks whether a player with the provided email already exists.
     *
     * @param email email to test
     * @return {@code true} when the email is already in use
     */
    public boolean isDuplicateUser(String email) {
        return gameSystem.getPlayers().emailExists(email);
    }

    /**
     * @return {@code true} if the player list contains duplicate email entries
     */
    public boolean hasDuplicateUsers() {
        return gameSystem.getPlayers().hasDuplicateUsers();
    }

    /**
     * @return players that share duplicate email addresses
     */
    public List<Player> getDuplicateUsers() {
        return gameSystem.getPlayers().findDuplicateUsers();
    }

    /**
     * Logs out the active player, syncing their progress and resetting
     * session-specific state such as the timer and puzzle flags.
     */
    public void logoutPlayer() {
        persistActiveProgress();
        activePlayer = null;
        gameSystem.getProgress().setActivePlayerId(null);
        gameSystem.getProgress().setCurrentRoomId(null);
        gameSystem.getProgress().clearSolved();
        clearAllPuzzleSolvedFlags();
        timerStarted = false;
        pauseTimerCountdown();
    }

    /**
     * Attempts to solve the specified puzzle with the provided answer.
     *
     * @param puzzleId identifier of the puzzle being answered
     * @param answer   proposed solution text
     * @return {@code true} when the answer was correct and state was updated; {@code false} otherwise
     */
    public boolean submitAnswer(UUID puzzleId, String answer) {
        if (puzzleId == null || answer == null) {
            return false;
        }
        Optional<Puzzle> puzzle = gameSystem.getPuzzles().findById(puzzleId);
        if (puzzle.isPresent() && puzzle.get().isCorrectAnswer(answer)) {
            gameSystem.getProgress().markPuzzleSolved(puzzleId);
            if (activePlayer != null) {
                activePlayer.addScore(POINTS_PER_PUZZLE);
                activePlayer.markPuzzleSolved(puzzleId);
                gameSystem.getLeaderboard().updateLeaderboard(activePlayer, activePlayer.getCurrentScore());
            }
            applyProgressToPuzzles();
            advanceToNextRoom(puzzleId);
            saveGame();
            return true;
        }
        return false;
    }

    /**
     * Finds the next unsolved puzzle and tries to solve it with the provided
     * answer.
     *
     * @param answer proposed solution for the next puzzle
     * @return optional puzzle that was solved; empty when no puzzle was solved
     */
    public Optional<Puzzle> solveNextPuzzle(String answer) {
        Optional<Puzzle> nextPuzzle = getNextUnsolvedPuzzle();
        if (nextPuzzle.isPresent() && submitAnswer(nextPuzzle.get().getId(), answer)) {
            return nextPuzzle;
        }
        return Optional.empty();
    }

    /**
     * Consumes the next available hint, applying the score penalty if there is
     * an active player.
     *
     * @return optional hint that was dispensed; empty when no hints remain
     */
    public Optional<Hint> useHint() {
        Hint hint = gameSystem.getHints().consumeNextHint();
        if (hint == null) {
            return Optional.empty();
        }
        if (activePlayer != null) {
            activePlayer.addScore(-HINT_PENALTY);
            gameSystem.getLeaderboard().updateLeaderboard(activePlayer, activePlayer.getCurrentScore());
        }
        return Optional.of(hint);
    }

    /**
     * @return the leaderboard maintained by the current game system
     */
    public Leaderboard getLeaderboard() {
        return gameSystem.getLeaderboard();
    }

    /**
     * @return the backing {@link GameSystem} instance
     */
    public GameSystem getGameSystem() {
        return gameSystem;
    }

    /**
     * @return optional active player for the session
     */
    public Optional<Player> getActivePlayer() {
        return Optional.ofNullable(activePlayer);
    }

    /**
     * @return the player list registered with this game
     */
    public PlayerList getPlayerList() {
        return gameSystem.getPlayers();
    }

    /**
     * @return unmodifiable list of the rooms currently in play
     */
    public List<Room> getRooms() {
        return Collections.unmodifiableList(getSequentialRooms());
    }

    /**
     * Retrieves a room by identifier within the sequential subset.
     *
     * @param roomId identifier of the desired room
     * @return optional matching room; empty when not available
     */
    public Optional<Room> getRoom(UUID roomId) {
        return getSequentialRooms().stream()
                .filter(room -> room.getId().equals(roomId))
                .findFirst();
    }

    /**
     * Attempts to enter the specified room if the player's progress allows it.
     *
     * @param roomId identifier of the room to enter
     * @return optional room that became current; empty when the transition was not permitted
     */
    public Optional<Room> enterRoom(UUID roomId) {
        ensureCurrentRoom();
        Optional<Room> requested = getRoom(roomId);
        if (requested.isEmpty()) {
            return Optional.empty();
        }

        Optional<Room> current = getCurrentRoom();
        if (current.isPresent() && current.get().getId().equals(roomId)) {
            return current;
        }

        Optional<Room> firstAvailable = findFirstAvailableRoom();
        if (firstAvailable.isPresent() && firstAvailable.get().getId().equals(roomId)) {
            gameSystem.getProgress().setCurrentRoomId(roomId);
            return firstAvailable;
        }
        return Optional.empty();
    }

    /**
     * @return the room currently assigned to the player's progress, if available
     */
    public Optional<Room> getCurrentRoom() {
        ensureCurrentRoom();
        UUID roomId = gameSystem.getProgress().getCurrentRoomId();
        if (roomId == null) {
            return Optional.empty();
        }
        return getRoom(roomId);
    }

    /**
     * @return the next unsolved puzzle from the current room sequence, if present
     */
    public Optional<Puzzle> getNextUnsolvedPuzzle() {
        Optional<Room> room = getCurrentRoom();
        if (room.isPresent()) {
            return room.get().getFirstUnsolvedPuzzle();
        }
        // If no room selected yet, use the first room with work remaining.
        return getRooms().stream()
                .map(Room::getFirstUnsolvedPuzzle)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private void setActivePlayer(Player player) {
        persistActiveProgress();
        this.activePlayer = player;
        gameSystem.getProgress().reset(player.getId());
        gameSystem.getProgress().loadSolvedPuzzles(player.getSolvedPuzzleIds());
        applyProgressToPuzzles();
        ensureCurrentRoom();
        timerStarted = false;
        configureSessionTimer();
    }

    private List<Room> getSequentialRooms() {
        List<Room> allRooms = gameSystem.getRooms().asList();
        List<Room> rooms = new ArrayList<>(Math.min(SEQUENTIAL_ROOM_LIMIT, allRooms.size()));
        for (int i = 0; i < allRooms.size() && i < SEQUENTIAL_ROOM_LIMIT; i++) {
            rooms.add(allRooms.get(i));
        }
        return rooms;
    }

    private Optional<Room> findFirstAvailableRoom() {
        return getSequentialRooms().stream()
                .filter(room -> room.getFirstUnsolvedPuzzle().isPresent())
                .findFirst();
    }

    private Optional<Room> findNextAvailableRoomAfter(Room currentRoom) {
        List<Room> rooms = getSequentialRooms();
        int index = rooms.indexOf(currentRoom);
        if (index < 0) {
            return Optional.empty();
        }
        for (int i = index + 1; i < rooms.size(); i++) {
            Room candidate = rooms.get(i);
            if (candidate.getFirstUnsolvedPuzzle().isPresent()) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private Optional<Room> findRoomByPuzzle(UUID puzzleId) {
        if (puzzleId == null) {
            return Optional.empty();
        }
        List<Room> rooms = getSequentialRooms();
        return gameSystem.getRooms().findByPuzzleId(puzzleId)
                .filter(rooms::contains);
    }

    private void ensureCurrentRoom() {
        List<Room> rooms = getSequentialRooms();
        if (rooms.isEmpty()) {
            gameSystem.getProgress().setCurrentRoomId(null);
            return;
        }

        UUID currentId = gameSystem.getProgress().getCurrentRoomId();
        Optional<Room> currentRoom = rooms.stream()
                .filter(room -> room.getId().equals(currentId))
                .findFirst();

        if (currentRoom.isPresent()) {
            if (currentRoom.get().getFirstUnsolvedPuzzle().isEmpty()) {
                Optional<Room> nextRoom = findNextAvailableRoomAfter(currentRoom.get());
                gameSystem.getProgress().setCurrentRoomId(nextRoom.map(Room::getId).orElse(null));
            }
            return;
        }

        Optional<Room> firstAvailable = findFirstAvailableRoom();
        gameSystem.getProgress().setCurrentRoomId(firstAvailable.map(Room::getId).orElse(null));
    }

    private void advanceToNextRoom(UUID puzzleId) {
        Optional<Room> currentRoom = findRoomByPuzzle(puzzleId);
        if (currentRoom.isEmpty()) {
            return;
        }
        Optional<Room> nextRoom = findNextAvailableRoomAfter(currentRoom.get());
        gameSystem.getProgress().setCurrentRoomId(nextRoom.map(Room::getId).orElse(null));
    }

    private void persistActiveProgress() {
        if (activePlayer == null) {
            return;
        }
        activePlayer.setSolvedPuzzleIds(gameSystem.getProgress().getSolvedPuzzleIds());
    }

    private void applyProgressToPuzzles() {
        Set<UUID> solved = gameSystem.getProgress().getSolvedPuzzleIds();
        for (Puzzle puzzle : gameSystem.getPuzzles().asList()) {
            if (solved.contains(puzzle.getId())) {
                puzzle.markSolved();
            } else {
                puzzle.reset();
            }
        }
    }

    /**
     * Configures the session timer and starts it if it is not already running.
     * The timer begins at the default duration on the first start.
     */
    public void startTimerCountdown() {
        configureSessionTimer();
        Timer timer = gameSystem.getTimer();
        if (timer == null) {
            return;
        }
        if (!timerStarted) {
            timer.setTotalTime(DEFAULT_TIMER_DURATION);
            timer.reset();
            timerStarted = true;
        }
        if (!timer.isRunning() && !timer.getRemaining().isZero()) {
            timer.start();
        }
    }

    /**
     * Pauses the session timer and clears the started flag when the countdown
     * has finished.
     */
    public void pauseTimerCountdown() {
        Timer timer = gameSystem.getTimer();
        if (timer == null) {
            return;
        }
        if (timer.isRunning()) {
            timer.pause();
        }
        if (timer.getRemaining().isZero()) {
            timerStarted = false;
        }
    }

    private void configureSessionTimer() {
        Timer timer = gameSystem.getTimer();
        if (timer == null) {
            timer = new Timer();
            gameSystem.setTimer(timer);
        }
        if (!timerStarted && !DEFAULT_TIMER_DURATION.equals(timer.getTotalTime())) {
            timer.setTotalTime(DEFAULT_TIMER_DURATION);
        }
        if (!timerStarted && timer.getRemaining().isZero()) {
            timer.reset();
        }
    }

    private void clearAllPuzzleSolvedFlags() {
        for (Puzzle puzzle : gameSystem.getPuzzles().asList()) {
            puzzle.reset();
        }
    }
}
