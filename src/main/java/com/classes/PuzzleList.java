package com.classes;

import java.util.ArrayList;
import java.util.List;

public class PuzzleList {
    private List<Player> players;
    
    public PlayerList() {
        players = new ArrayList();
    }

    public void addPlayer(Player p) {
        players.add(p);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }
}
