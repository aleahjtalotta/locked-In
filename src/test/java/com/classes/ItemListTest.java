package com.classes;

import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ItemListTest {

    private Item createItem(String name) {
        return new Item(UUID.randomUUID(), 1L, name, true);
    }

    @Test
    public void addStoresNonNullItem() {
        ItemList list = new ItemList();
        Item item = createItem("Key");
        list.add(item);
        assertEquals(1, list.size());
        assertSame(item, list.asList().get(0));
    }

    @Test
    public void addIgnoresNullItem() {
        ItemList list = new ItemList();
        list.add(null);
        assertEquals(0, list.size());
    }

    @Test
    public void removeReturnsTrueWhenItemPresent() {
        ItemList list = new ItemList();
        Item item = createItem("Key");
        list.add(item);
        assertTrue(list.remove(item));
        assertEquals(0, list.size());
    }

    @Test
    public void removeReturnsFalseWhenItemMissing() {
        ItemList list = new ItemList();
        Item item = createItem("Key");
        assertFalse(list.remove(item));
    }

    @Test
    public void findByIdReturnsMatchingItem() {
        ItemList list = new ItemList();
        Item item = createItem("Key");
        list.add(item);
        Optional<Item> result = list.findById(item.getId());
        assertTrue(result.isPresent());
        assertSame(item, result.get());
    }

    @Test
    public void findByIdReturnsEmptyWhenNotFound() {
        ItemList list = new ItemList();
        list.add(createItem("Key"));
        assertTrue(list.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    public void asListReturnsUnmodifiableView() {
        ItemList list = new ItemList();
        list.add(createItem("Key"));
        try {
            list.asList().add(createItem("Map"));
            org.junit.Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ignored) {
            // expected
        }
    }

    @Test
    public void asListPreservesInsertionOrder() {
        ItemList list = new ItemList();
        Item first = createItem("Key");
        Item second = createItem("Map");
        list.add(first);
        list.add(second);
        List<Item> items = list.asList();
        assertSame(first, items.get(0));
        assertSame(second, items.get(1));
    }

    @Test
    public void sizeReturnsNumberOfItems() {
        ItemList list = new ItemList();
        list.add(createItem("Key"));
        list.add(createItem("Map"));
        assertEquals(2, list.size());
    }
}
