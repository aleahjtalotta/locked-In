package com.classes;
import java.util.ArrayList;
import java.util.List;

public class ItemList {
    
    private List<Item> items;

    public ItemList() {
        items = new ArrayList<>();
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public Item getItem(int index) {
        return items.get(index);
    }
}
