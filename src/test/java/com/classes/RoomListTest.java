package com.classes;

import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RoomListTest {

    private Room createRoom() {
        return new Room(UUID.randomUUID(), 1);
    }

    private Puzzle createPuzzle(UUID id) {
        return new WriteInPuzzle(
                id,
                1L,
                "Answer Me",
                "Provide the secret word.",
                "A clue",
                "secret",
                false
        );
    }

    @Test
    public void addStoresNonNullRoom() {
        RoomList list = new RoomList();
        Room room = createRoom();
        list.add(room);
        assertEquals(1, list.size());
        assertSame(room, list.asList().get(0));
    }

    @Test
    public void addIgnoresNullRoom() {
        RoomList list = new RoomList();
        list.add(null);
        assertEquals(0, list.size());
    }

    @Test
    public void asListReturnsUnmodifiableView() {
        RoomList list = new RoomList();
        list.add(createRoom());
        try {
            list.asList().add(createRoom());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ignored) {
            // expected
        }
    }

    @Test
    public void findByIdReturnsRoomWithMatchingId() {
        RoomList list = new RoomList();
        Room room = createRoom();
        list.add(room);
        Optional<Room> result = list.findById(room.getId());
        assertTrue(result.isPresent());
        assertSame(room, result.get());
    }

    @Test
    public void findByIdReturnsEmptyWhenIdNotFound() {
        RoomList list = new RoomList();
        list.add(createRoom());
        assertTrue(list.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    public void sizeReflectsRoomCount() {
        RoomList list = new RoomList();
        list.add(createRoom());
        list.add(createRoom());
        assertEquals(2, list.size());
    }

    @Test
    public void findByPuzzleIdReturnsRoomContainingPuzzle() {
        RoomList list = new RoomList();
        Room roomWithPuzzle = createRoom();
        UUID puzzleId = UUID.randomUUID();
        roomWithPuzzle.addPuzzle(createPuzzle(puzzleId));
        Room otherRoom = createRoom();
        list.add(otherRoom);
        list.add(roomWithPuzzle);

        Optional<Room> result = list.findByPuzzleId(puzzleId);
        assertTrue(result.isPresent());
        assertSame(roomWithPuzzle, result.get());
    }

    @Test
    public void findByPuzzleIdReturnsEmptyWhenPuzzleMissing() {
        RoomList list = new RoomList();
        Room room = createRoom();
        room.addPuzzle(createPuzzle(UUID.randomUUID()));
        list.add(room);
        assertTrue(list.findByPuzzleId(UUID.randomUUID()).isEmpty());
    }

    @Test
    public void findByPuzzleIdReturnsEmptyWhenPuzzleIdNull() {
        RoomList list = new RoomList();
        Room room = createRoom();
        room.addPuzzle(createPuzzle(UUID.randomUUID()));
        list.add(room);
        assertTrue(list.findByPuzzleId(null).isEmpty());
    }

    @Test
    public void asListPreservesInsertionOrder() {
        RoomList list = new RoomList();
        Room first = createRoom();
        Room second = createRoom();
        list.add(first);
        list.add(second);
        List<Room> rooms = list.asList();
        assertSame(first, rooms.get(0));
        assertSame(second, rooms.get(1));
    }
}
