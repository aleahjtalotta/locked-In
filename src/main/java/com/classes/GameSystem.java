package com.classes;

import java.util.Objects;
import java.util.UUID;

public class GameSystem {
    private final UUID id;
    private Long legacyId;
    private RoomList rooms;
    private PuzzleList puzzles;
    private PlayerList players;
    private Leaderboard leaderboard;
    private Hints hints;
    private Timer timer;
    private DifficultyLevel difficulty;
    private Progress progress;

    public GameSystem() {
        this(UUID.randomUUID());
    }

    public GameSystem(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
        this.legacyId = null;
        this.rooms = new RoomList();
        this.puzzles = new PuzzleList();
        this.players = new PlayerList();
        this.leaderboard = new Leaderboard();
        this.hints = new Hints();
        this.timer = new Timer();
        this.difficulty = DifficultyLevel.MEDIUM;
        this.progress = new Progress();
    }

    public UUID getId() {
        return id;
    }

    public Long getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Long legacyId) {
        this.legacyId = legacyId;
    }

    public RoomList getRooms() {
        return rooms;
    }

    public void setRooms(RoomList rooms) {
        this.rooms = Objects.requireNonNullElse(rooms, new RoomList());
    }

    public PuzzleList getPuzzles() {
        return puzzles;
    }

    public void setPuzzles(PuzzleList puzzles) {
        this.puzzles = Objects.requireNonNullElse(puzzles, new PuzzleList());
    }

    public PlayerList getPlayers() {
        return players;
    }

    public void setPlayers(PlayerList players) {
        this.players = Objects.requireNonNullElse(players, new PlayerList());
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(Leaderboard leaderboard) {
        this.leaderboard = Objects.requireNonNullElse(leaderboard, new Leaderboard());
    }

    public Hints getHints() {
        return hints;
    }

    public void setHints(Hints hints) {
        this.hints = Objects.requireNonNullElse(hints, new Hints());
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = Objects.requireNonNullElse(timer, new Timer());
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = Objects.requireNonNullElse(difficulty, DifficultyLevel.MEDIUM);
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = Objects.requireNonNullElse(progress, new Progress());
    }
}
