package com.classes;

import java.io.FileReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.ItemList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


    public class DataLoader {
        private final String source;

        public DataLoader(String source) {
            this.source = source;
        }

    public GameSystem loadGame(){
        GameSystem game = new GameSystem();
        PuzzleList puzzles = loadPuzzles();
        PlayerList players = loadPlayers();
        RoomList rooms = loadRooms(puzzles);
        Leaderboard leaderboard = loadLeaderboard();
        game.setPuzzles(puzzles);
        game.setPlayers(players);
        game.setRooms(rooms);
        game.setLeaderboard(leaderboard);
        game.setHints(new Hints());
        game.setTimer(new Timer());
        return game;
    }
    public PlayerList loadPlayers() {
        PlayerList players = new PlayerList();

        JSONParser parser = new JSONParser();
        String path = source + "/players.json";
        try (FileReader reader = new FileReader(path)) {
            JSONArray arr = (JSONArray) parser.parse(reader);
            for (Object o : arr) {
                JSONObject jo = (JSONObject) o;
                String name = (String) jo.getOrDefault("name", "");
                String email = (String) jo.getOrDefault("email", "");

                player p = new Player(name, email);
                player.addPlayer(p);
                    
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
            
     return players;

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
public Leaderboard loadLeaderboard() { 
 Leaderboard lb = new Leaderboard();

 JSONParser parser = new JSONParser() {
    String path = source + ""; // JSON?
    try (FileReader reader = new FileReader(path)) {
        JSONArray arr = (JSONArray) parser.parse(reader);
        for (Object o : arr) {
            JSONObject jo = (JSONObject) o;
            String playerName = (String)
            jo.getOrDefault("playerName","");
            int score = (int) (((Number)

            jo.getOrDefault("score", 0)).longValue());
            Duration dur = Duration.ofSeconds(seconds);
            ScoreEntry entry = new ScoreEntry(playerName, score, dur);
            lb.addScoreEntry(entry);
        }
    }catch(Exception e) {
        e.printStackTrace();
    
    }
    return lb;
 }
}





    } catch (Exception e) {

}    


