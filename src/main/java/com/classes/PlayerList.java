package com.classes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

/**
 * Manages the collection of players available to the game.
 */
public class PlayerList {
    private final List<Player> players;

    /**
     * Creates an empty, mutable list of players.
     */
    public PlayerList() {
        this.players = new ArrayList<>();
    }

    /**
     * Adds a player to the list when the reference is non-null.
     *
     * @param player player to add
     */
    public void add(Player player) {
        if (player != null) {
            players.add(player);
        }
    }

    /**
     * Removes the specified player instance from the list.
     *
     * @param player player to remove
     * @return {@code true} if the list contained the player
     */
    public boolean remove(Player player) {
        return players.remove(player);
    }

    /**
     * Finds a player by their unique identifier.
     *
     * @param id identifier to search for
     * @return matching player, if present
     */
    public Optional<Player> findById(UUID id) {
        return players.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    /**
     * Looks up a player by email address, ignoring case and surrounding whitespace.
     *
     * @param email email address to match
     * @return player whose normalized email matches, if any
     */
    public Optional<Player> findByEmail(String email) {
        String normalized = normalizeEmail(email);
        if (normalized == null) {
            return Optional.empty();
        }
        return players.stream()
                .filter(p -> {
                    String candidate = normalizeEmail(p.getEmail());
                    return normalized.equals(candidate);
                })
                .findFirst();
    }

    /**
     * Checks whether a player already uses the supplied email address.
     *
     * @param email email to check
     * @return {@code true} if the email is associated with an existing player
     */
    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    /**
     * Creates and stores a new player, validating email uniqueness and auto-assigning a legacy id.
     *
     * @param name   desired player name
     * @param email  unique email address
     * @param avatar optional avatar reference
     * @return newly created player
     * @throws IllegalArgumentException when the email is missing or already in use
     */
    public Player createPlayer(String name, String email, String avatar) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (emailExists(email)) {
            throw new IllegalArgumentException("A player with that email already exists.");
        }
        String safeName = (name == null || name.isBlank()) ? "New Player" : name.trim();
        int nextLegacy = players.stream()
                .map(Player::getLegacyId)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
        UUID playerId = deriveUuid("player", nextLegacy);
        Player player = new Player(playerId, nextLegacy, safeName, email.trim(), avatar,
                new ItemList(), new Statistics(), 0, Collections.emptySet());
        players.add(player);
        return player;
    }

    /**
     * Determines whether any players share the same normalized email address.
     *
     * @return {@code true} if duplicate emails are discovered
     */
    public boolean hasDuplicateUsers() {
        Set<String> emails = new HashSet<>();
        for (Player player : players) {
            String normalized = normalizeEmail(player.getEmail());
            if (normalized == null) {
                continue;
            }
            if (!emails.add(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects all players that share a duplicate email address with at least one other player.
     *
     * @return list of players involved in duplicate email collisions
     */
    public List<Player> findDuplicateUsers() {
        Map<String, List<Player>> groupedByEmail = new LinkedHashMap<>();
        for (Player player : players) {
            String normalized = normalizeEmail(player.getEmail());
            if (normalized == null) {
                continue;
            }
            groupedByEmail.computeIfAbsent(normalized, key -> new ArrayList<>()).add(player);
        }
        List<Player> duplicates = new ArrayList<>();
        for (List<Player> group : groupedByEmail.values()) {
            if (group.size() > 1) {
                duplicates.addAll(group);
            }
        }
        return duplicates;
    }

    /**
     * Returns an immutable snapshot of the players in insertion order.
     *
     * @return unmodifiable player list
     */
    public List<Player> asList() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Reports the number of registered players.
     *
     * @return player count
     */
    public int size() {
        return players.size();
    }

    /**
     * Checks whether a player already uses the provided name.
     *
     * @param name player name to check
     * @return {@code true} if the name is already taken
     */
    public boolean nameExists(String name) {
        String normalized = normalizeName(name);
        if (normalized == null) {
            return false;
        }
        return players.stream()
                .map(Player::getName)
                .map(this::normalizeName)
                .anyMatch(normalized::equals);
    }

    /**
     * Normalizes email strings by trimming whitespace and converting to lower case.
     *
     * @param email raw email input
     * @return normalized email or {@code null} when the input is blank
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normalizes names by trimming whitespace and converting to lower case.
     *
     * @param name raw name input
     * @return normalized name or {@code null} when the input is blank
     */
    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private UUID deriveUuid(String prefix, Number id) {
        if (id == null) {
            return UUID.randomUUID();
        }
        String seed = prefix + "-" + id.longValue();
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
