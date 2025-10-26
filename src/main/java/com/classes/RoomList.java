package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tiny helper that just keeps a bunch of rooms together for the game.
 * Comments are written the way I'd explain it to a friend.
 */
public class RoomList {
    private final List<Room> rooms;

    /**
     * Starts with an empty list so we can add rooms later.
     */
    public RoomList() {
        this.rooms = new ArrayList<>();
    }

    /**
     * Drops a room into the list when it is not null.
     *
     * @param room room we want to track
     */
    public void add(Room room) {
        if (room != null) {
            rooms.add(room);
        }
    }

    /**
     * @return read-only view so people can see rooms without changing them
     */
    public List<Room> asList() {
        return Collections.unmodifiableList(rooms);
    }

    /**
     * Looks for a room with the given id.
     *
     * @param id uuid we are trying to match
     * @return a room wrapped in optional if we find one
     */
    public Optional<Room> findById(UUID id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    /**
     * @return how many rooms are stored right now
     */
    public int size() {
        return rooms.size();
    }

    /**
     * Finds the first room that contains a specific puzzle.
     *
     * @param puzzleId puzzle we are hunting for; null just returns empty
     * @return room that holds a puzzle with that id if one exists
     */
    public Optional<Room> findByPuzzleId(UUID puzzleId) {
        if (puzzleId == null) {
            return Optional.empty();
        }
        return rooms.stream()
                .filter(room -> room.findPuzzle(puzzleId).isPresent())
                .findFirst();
    }
}
