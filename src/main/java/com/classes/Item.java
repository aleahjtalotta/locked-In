package com.classes;

import java.util.List;
import java.util.ArrayList;

public class Item extends ItemList{
    private int RoomID;
    protected List<Puzzle> puzzles;
    protected List<Item> items;

    public Item(int RoomID) {
        this.RoomID = RoomID;
        this.puzzles = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public int getRoomID() {
        return RoomID;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void enterRoom() {
        //
    }

    public void completeRoom() {
        //
    }

    public void unlockNextPuzzle() {
        //
    }

}
