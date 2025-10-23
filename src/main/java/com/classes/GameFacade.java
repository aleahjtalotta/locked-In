package com.classes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * High-level API for interacting with the game state.
 */
public class GameFacade {
    private GameSystem gameSystem;
    private final DataLoader dataLoader;
    private final DataWriter dataWriter;
    private Player activePlayer;
    private static final int SEQUENTIAL_ROOM_LIMIT = 3;

    public GameFacade(String dataDirectory) {
        Objects.requireNonNull(dataDirectory, "dataDirectory");
        Path basePath = Path.of(dataDirectory);
        this.dataLoader = new DataLoader(basePath);
        this.dataWriter = new DataWriter(basePath);
        this.gameSystem = new GameSystem();
    }

    public void startNewGame() {
        this.gameSystem = new GameSystem();
        this.activePlayer = null;
        ensureCurrentRoom();
    }

    public boolean loadGame() {
        return dataLoader.loadGame().map(loaded -> {
            this.gameSystem = loaded;
            this.activePlayer = null;
            ensureCurrentRoom();
            return true;
        }).orElse(false);
    }

    public boolean saveGame() {
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
        activePlayer = null;
        gameSystem.getProgress().setActivePlayerId(null);
        gameSystem.getProgress().setCurrentRoomId(null);
    }

    public boolean submitAnswer(UUID puzzleId, String answer) {
        if (puzzleId == null || answer == null) {
            return false;
        }
        Optional<Puzzle> puzzle = gameSystem.getPuzzles().findById(puzzleId);
        if (puzzle.isPresent() && puzzle.get().isCorrectAnswer(answer)) {
            puzzle.get().markSolved();
            gameSystem.getProgress().markPuzzleSolved(puzzleId);
            if (activePlayer != null) {
                activePlayer.addScore(100);
            }
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
        this.activePlayer = player;
        gameSystem.getProgress().reset(player.getId());
        ensureCurrentRoom();
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
}
