package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        if (email == null) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return players.stream()
                .filter(p -> {
                    String candidate = p.getEmail() == null ? "" : p.getEmail().trim().toLowerCase();
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
        Player player = new Player(UUID.randomUUID(), nextLegacy, safeName, email.trim(), avatar, new ItemList(), new Statistics(), 0);
        players.add(player);
        return player;
    }

    public List<Player> asList() {
        return Collections.unmodifiableList(players);
    }

    public int size() {
        return players.size();
    }
}
