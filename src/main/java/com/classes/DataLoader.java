package com.classes;

public class DataLoader {
    private String source;

    public DataLoader(String source) {
        this.source = source;
    }

    public GameSystem loadGame(){
        GameSystem gs = new gameSystem();
        
        PlayerList players = loadPlayers();
        PuzzleList puzzles = loadPuzzles();
        RoomList roomks = loadRooms();
        ItemList items = loadItems();

        return null;
    }

    public PlayerList loadPlayers(){
        return null;
    }

    public PuzzleList loadPuzzles() {
        return null;
    }
    
    public RoomList loadRooms(){
        return null;
    }

    public ItemList loadItems(){
        return null;
    }








    } catch (Exception e) {

}    


