package com.classes;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root that bundles all mutable escape-room state. A {@code GameSystem}
 * instance owns the rooms, puzzles, players, leaderboard, hints, timer,
 * difficulty, and progress objects so other layers can load, persist, and
 * present a cohesive game session.
 */
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

    /**
     * Creates a fresh game system with a randomly generated identifier and
     * empty collections for rooms, puzzles, players, leaderboard, hints, timer,
     * and progress.
     */
    public GameSystem() {
        this(UUID.randomUUID());
    }

    /**
     * Creates a game system anchored to a specific identifier.
     *
     * @param id immutable identifier for this game session; must not be {@code null}
     */
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

    /**
     * @return immutable identifier for this game session
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return optional numeric identifier from legacy data sources, or {@code null} when absent
     */
    public Long getLegacyId() {
        return legacyId;
    }

    /**
     * Overrides the legacy numeric identifier linked with this session.
     *
     * @param legacyId legacy identifier to associate; may be {@code null}
     */
    public void setLegacyId(Long legacyId) {
        this.legacyId = legacyId;
    }

    /**
     * @return mutable list container describing all rooms in the escape room
     */
    public RoomList getRooms() {
        return rooms;
    }

    /**
     * Replaces the room list backing this game system.
     *
     * @param rooms new room list; {@code null} yields an empty {@link RoomList}
     */
    public void setRooms(RoomList rooms) {
        this.rooms = Objects.requireNonNullElse(rooms, new RoomList());
    }

    /**
     * @return registry of every puzzle available to the game
     */
    public PuzzleList getPuzzles() {
        return puzzles;
    }

    /**
     * Updates the puzzle registry for this session.
     *
     * @param puzzles new puzzle list; {@code null} yields an empty {@link PuzzleList}
     */
    public void setPuzzles(PuzzleList puzzles) {
        this.puzzles = Objects.requireNonNullElse(puzzles, new PuzzleList());
    }

    /**
     * @return collection of players known to the game
     */
    public PlayerList getPlayers() {
        return players;
    }

    /**
     * Sets the player list that participates in this session.
     *
     * @param players new player list; {@code null} yields an empty {@link PlayerList}
     */
    public void setPlayers(PlayerList players) {
        this.players = Objects.requireNonNullElse(players, new PlayerList());
    }

    /**
     * @return leaderboard tracking player scores
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    /**
     * Replaces the leaderboard for this session.
     *
     * @param leaderboard new leaderboard; {@code null} yields a fresh {@link Leaderboard}
     */
    public void setLeaderboard(Leaderboard leaderboard) {
        this.leaderboard = Objects.requireNonNullElse(leaderboard, new Leaderboard());
    }

    /**
     * @return hint queue available to players
     */
    public Hints getHints() {
        return hints;
    }

    /**
     * Updates the hint collection managed by this session.
     *
     * @param hints new hints instance; {@code null} yields a fresh {@link Hints}
     */
    public void setHints(Hints hints) {
        this.hints = Objects.requireNonNullElse(hints, new Hints());
    }

    /**
     * @return timer used to pace the escape-room session
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Sets the timer responsible for session countdowns.
     *
     * @param timer new timer; {@code null} yields a fresh {@link Timer}
     */
    public void setTimer(Timer timer) {
        this.timer = Objects.requireNonNullElse(timer, new Timer());
    }

    /**
     * @return difficulty level that governs puzzle configuration
     */
    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    /**
     * Assigns the difficulty for the current session.
     *
     * @param difficulty desired difficulty; {@code null} falls back to {@link DifficultyLevel#MEDIUM}
     */
    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = Objects.requireNonNullElse(difficulty, DifficultyLevel.MEDIUM);
    }

    /**
     * @return progress tracker describing the active run-through
     */
    public Progress getProgress() {
        return progress;
    }

    /**
     * Replaces the progress tracker for this session.
     *
     * @param progress new progress object; {@code null} yields a fresh {@link Progress}
     */
    public void setProgress(Progress progress) {
        this.progress = Objects.requireNonNullElse(progress, new Progress());
    }
}
