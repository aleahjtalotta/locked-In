package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RoomList {
    private final List<Room> rooms;

    public RoomList() {
        this.rooms = new ArrayList<>();
    }

    public void add(Room room) {
        if (room != null) {
            rooms.add(room);
        }
    }

    public List<Room> asList() {
        return Collections.unmodifiableList(rooms);
    }

    public Optional<Room> findById(UUID id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public int size() {
        return rooms.size();
    }
}
