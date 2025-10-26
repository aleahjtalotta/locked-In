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

    public ItemList() {
        this.items = new ArrayList<>();
    }

    public void add(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    public boolean remove(Item item) {
        return items.remove(item);
    }

    public Optional<Item> findById(UUID id) {
        return items.stream().filter(it -> it.getId().equals(id)).findFirst();
    }

    public List<Item> asList() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }
}
