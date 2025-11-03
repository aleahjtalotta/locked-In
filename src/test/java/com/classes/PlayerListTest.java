package com.classes;

import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PlayerListTest {

    private Player createPlayer(String name, String email, Integer legacyId) {
        return new Player(
                UUID.randomUUID(),
                legacyId,
                name,
                email,
                null,
                new ItemList(),
                new Statistics(),
                0,
                null
        );
    }

    @Test
    public void addStoresNonNullPlayer() {
        PlayerList list = new PlayerList();
        Player player = createPlayer("Ada", "ada@example.com", 1);

        list.add(player);

        assertEquals(1, list.size());
        assertSame(player, list.asList().get(0));
    }

    @Test
    public void addIgnoresNullPlayer() {
        PlayerList list = new PlayerList();

        list.add(null);

        assertEquals(0, list.size());
    }

    @Test
    public void removeReturnsTrueWhenPlayerPresent() {
        PlayerList list = new PlayerList();
        Player player = createPlayer("Ada", "ada@example.com", 1);
        list.add(player);

        assertTrue(list.remove(player));
        assertEquals(0, list.size());
    }

    @Test
    public void removeReturnsFalseWhenPlayerMissing() {
        PlayerList list = new PlayerList();
        Player player = createPlayer("Ada", "ada@example.com", 1);

        assertFalse(list.remove(player));
    }

    @Test
    public void findByIdReturnsMatchingPlayer() {
        PlayerList list = new PlayerList();
        Player player = createPlayer("Ada", "ada@example.com", 1);
        list.add(player);

        Optional<Player> result = list.findById(player.getId());

        assertTrue(result.isPresent());
        assertSame(player, result.get());
    }

    @Test
    public void findByEmailMatchesIgnoringCaseAndWhitespace() {
        PlayerList list = new PlayerList();
        Player player = createPlayer("Ada", "Ada@Example.com", 1);
        list.add(player);

        Optional<Player> result = list.findByEmail(" ada@example.com ");

        assertTrue(result.isPresent());
        assertSame(player, result.get());
    }

    @Test
    public void findByEmailReturnsEmptyForNullOrUnknownEmail() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", "ada@example.com", 1));

        assertTrue(list.findByEmail(null).isEmpty());
        assertTrue(list.findByEmail("nobody@example.com").isEmpty());
    }

    @Test
    public void emailExistsReturnsTrueWhenPlayerFound() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", "ada@example.com", 1));

        assertTrue(list.emailExists("Ada@example.com"));
        assertFalse(list.emailExists("nope@example.com"));
    }

    @Test
    public void createPlayerRegistersNewPlayerWhenEmailUnique() {
        PlayerList list = new PlayerList();

        Player created = list.createPlayer(" Ada Lovelace ", " ada@example.com ", "avatar.png");

        assertEquals(1, list.size());
        assertEquals("Ada Lovelace", created.getName());
        assertEquals("ada@example.com", created.getEmail());
        assertEquals(Integer.valueOf(1), created.getLegacyId());
    }

    @Test
    public void createPlayerDefaultsNameWhenBlank() {
        PlayerList list = new PlayerList();

        Player created = list.createPlayer(" ", "ada@example.com", null);

        assertEquals("New Player", created.getName());
    }

    @Test
    public void createPlayerAssignsIncrementingLegacyIds() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Existing", "existing@example.com", 5));

        Player created = list.createPlayer("New Person", "new@example.com", null);

        assertEquals(Integer.valueOf(6), created.getLegacyId());
    }

    @Test
    public void createPlayerRejectsDuplicateEmail() {
        PlayerList list = new PlayerList();
        list.createPlayer("Ada", "ada@example.com", null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> list.createPlayer("Grace", "Ada@example.com", null)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    public void createPlayerRejectsBlankEmail() {
        PlayerList list = new PlayerList();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> list.createPlayer("Ada", " ", null)
        );

        assertTrue(ex.getMessage().contains("Email is required"));
    }

    @Test
    public void createPlayerRejectsDuplicateNameIgnoringCase() {
        PlayerList list = new PlayerList();
        list.createPlayer("Ada Lovelace", "ada@example.com", null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> list.createPlayer(" ada lovelace ", "second@example.com", null)
        );

        assertTrue(ex.getMessage().contains("name already exists"));
    }

    @Test
    public void hasDuplicateUsersReturnsTrueWhenEmailsRepeated() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", "ada@example.com", 1));
        list.add(createPlayer("Grace", "ADA@example.com", 2));

        assertTrue(list.hasDuplicateUsers());
    }

    @Test
    public void hasDuplicateUsersIgnoresNullOrBlankEmails() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", null, 1));
        list.add(createPlayer("Grace", " ", 2));

        assertFalse(list.hasDuplicateUsers());
    }

    @Test
    public void findDuplicateUsersReturnsAllPlayersSharingEmail() {
        PlayerList list = new PlayerList();
        Player first = createPlayer("Ada", "ada@example.com", 1);
        Player second = createPlayer("Grace", "ADA@example.com", 2);
        Player third = createPlayer("Linus", "linus@example.com", 3);
        Player fourth = createPlayer("Marie", "linus@example.com", 4);
        list.add(first);
        list.add(second);
        list.add(third);
        list.add(fourth);

        List<Player> duplicates = list.findDuplicateUsers();

        assertEquals(4, duplicates.size());
        assertTrue(duplicates.contains(first));
        assertTrue(duplicates.contains(second));
        assertTrue(duplicates.contains(third));
        assertTrue(duplicates.contains(fourth));
    }

    @Test
    public void findDuplicateUsersReturnsEmptyWhenNoCollisions() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", "ada@example.com", 1));
        list.add(createPlayer("Grace", "grace@example.com", 2));

        assertTrue(list.findDuplicateUsers().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void asListReturnsUnmodifiableView() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", "ada@example.com", 1));

        list.asList().add(createPlayer("Grace", "grace@example.com", 2));
    }

    @Test
    public void asListPreservesInsertionOrder() {
        PlayerList list = new PlayerList();
        Player first = createPlayer("Ada", "ada@example.com", 1);
        Player second = createPlayer("Grace", "grace@example.com", 2);
        list.add(first);
        list.add(second);

        List<Player> players = list.asList();

        assertSame(first, players.get(0));
        assertSame(second, players.get(1));
    }

    @Test
    public void sizeReflectsNumberOfPlayers() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada", "ada@example.com", 1));
        list.add(createPlayer("Grace", "grace@example.com", 2));

        assertEquals(2, list.size());
    }

    @Test
    public void nameExistsMatchesIgnoringCaseAndWhitespace() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada Lovelace", "ada@example.com", 1));

        assertTrue(list.nameExists(" ada lovelace "));
        assertFalse(list.nameExists("Grace Hopper"));
    }

    @Test
    public void nameExistsReturnsFalseForBlankName() {
        PlayerList list = new PlayerList();
        list.add(createPlayer("Ada Lovelace", "ada@example.com", 1));

        assertFalse(list.nameExists(" "));
        assertFalse(list.nameExists(null));
    }
}
