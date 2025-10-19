package com.classes;

import org.json.simple.ItemList;

public class Player extends PlayerList{
    
    private int playerID;
    private String name;
    private String email;
    private ItemList items;
    private Statistics statistics;
    private int currentScore;


    public Player(int playerID, String name, String email) {
        this.playerID = playerID;
        this.name = name;
        this.email = email;
        this.items = new ItemList();
        this.statistics = new Statistics();
        this.currentScore = 0;
    }

    public void login(String name , String email) {
        this.name = name;
        this.email = email;
    }

    public void logout() {
        this.name = null;
        this.email = null;
    }

    public void collectItem(Item item) {
        items.addItem(item);
    }
    public Player getPlayer() {
        return this;
    }
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void updateScore(int score) {
        this.currentScore += score;
    }

}
