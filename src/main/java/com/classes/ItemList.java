package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Maintains the set of items owned by a player.
 */
public class ItemList {
    private final List<Item> items;

    /**
     * Creates an empty list ready to hold item instances.
     */
    public ItemList() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds an item when the reference is not {@code null}.
     *
     * @param item item to include in the list
     */
    public void add(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Removes a specific item instance from the list.
     *
     * @param item item to remove
     * @return {@code true} if the list contained the item
     */
    public boolean remove(Item item) {
        return items.remove(item);
    }

    /**
     * Finds an item by its unique identifier.
     *
     * @param id identifier to locate
     * @return matching item wrapped in {@link Optional}
     */
    public Optional<Item> findById(UUID id) {
        return items.stream().filter(it -> it.getId().equals(id)).findFirst();
    }

    /**
     * Returns an immutable view of the contained items.
     *
     * @return unmodifiable list of items
     */
    public List<Item> asList() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Reports how many items are stored in the list.
     *
     * @return item count
     */
    public int size() {
        return items.size();
    }
}
