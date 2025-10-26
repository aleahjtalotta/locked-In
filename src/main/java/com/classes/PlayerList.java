package com.classes;

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

    public PlayerList() {
        this.players = new ArrayList<>();
    }

    public void add(Player player) {
        if (player != null) {
            players.add(player);
        }
    }

    public boolean remove(Player player) {
        return players.remove(player);
    }

    public Optional<Player> findById(UUID id) {
        return players.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

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

    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

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
        Player player = new Player(UUID.randomUUID(), nextLegacy, safeName, email.trim(), avatar,
                new ItemList(), new Statistics(), 0, Collections.emptySet());
        players.add(player);
        return player;
    }

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

    public List<Player> asList() {
        return Collections.unmodifiableList(players);
    }

    public int size() {
        return players.size();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }
}
