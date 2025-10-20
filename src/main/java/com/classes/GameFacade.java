package com.classes;

import java.nio.file.Path;
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
    }

    public boolean loadGame() {
        return dataLoader.loadGame().map(loaded -> {
            this.gameSystem = loaded;
            this.activePlayer = null;
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
        return gameSystem.getRooms().asList();
    }

    public Optional<Room> getRoom(UUID roomId) {
        return gameSystem.getRooms().findById(roomId);
    }

    public Optional<Room> enterRoom(UUID roomId) {
        Optional<Room> room = getRoom(roomId);
        room.ifPresent(value -> gameSystem.getProgress().setCurrentRoomId(value.getId()));
        return room;
    }

    public Optional<Room> getCurrentRoom() {
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
    }
}
