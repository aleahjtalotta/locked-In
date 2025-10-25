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

    public GameFacade(String dataDirectory) {
        Objects.requireNonNull(dataDirectory, "dataDirectory");
        Path basePath = Path.of(dataDirectory);
        this.dataLoader = new DataLoader(basePath);
        this.dataWriter = new DataWriter(basePath);
        this.gameSystem = new GameSystem();
        this.timerStarted = false;
    }

    public void startNewGame() {
        this.gameSystem = new GameSystem();
        this.activePlayer = null;
        this.gameSystem.getProgress().clearSolved();
        clearAllPuzzleSolvedFlags();
        ensureCurrentRoom();
        timerStarted = false;
        configureSessionTimer();
    }

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

    public boolean saveGame() {
        persistActiveProgress();
        return dataWriter.saveGame(gameSystem);
    }

    public boolean loginPlayer(UUID playerId) {
        Optional<Player> player = gameSystem.getPlayers().findById(playerId);
        player.ifPresent(this::setActivePlayer);
        return player.isPresent();
    }

    public boolean login(String email) {
        Optional<Player> player = gameSystem.getPlayers().findByEmail(email);
        player.ifPresent(this::setActivePlayer);
        return player.isPresent();
    }

    public Player createAccount(String name, String email, String avatar) {
        Player player = gameSystem.getPlayers().createPlayer(name, email, avatar);
        saveGame();
        return player;
    }

    public boolean isDuplicateUser(String email) {
        return gameSystem.getPlayers().emailExists(email);
    }

    public boolean hasDuplicateUsers() {
        return gameSystem.getPlayers().hasDuplicateUsers();
    }

    public List<Player> getDuplicateUsers() {
        return gameSystem.getPlayers().findDuplicateUsers();
    }

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

    public Optional<Puzzle> solveNextPuzzle(String answer) {
        Optional<Puzzle> nextPuzzle = getNextUnsolvedPuzzle();
        if (nextPuzzle.isPresent() && submitAnswer(nextPuzzle.get().getId(), answer)) {
            return nextPuzzle;
        }
        return Optional.empty();
    }

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

    public Leaderboard getLeaderboard() {
        return gameSystem.getLeaderboard();
    }

    public GameSystem getGameSystem() {
        return gameSystem;
    }

    public Optional<Player> getActivePlayer() {
        return Optional.ofNullable(activePlayer);
    }

    public PlayerList getPlayerList() {
        return gameSystem.getPlayers();
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(getSequentialRooms());
    }

    public Optional<Room> getRoom(UUID roomId) {
        return getSequentialRooms().stream()
                .filter(room -> room.getId().equals(roomId))
                .findFirst();
    }

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

    public Optional<Room> getCurrentRoom() {
        ensureCurrentRoom();
        UUID roomId = gameSystem.getProgress().getCurrentRoomId();
        if (roomId == null) {
            return Optional.empty();
        }
        return getRoom(roomId);
    }

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
