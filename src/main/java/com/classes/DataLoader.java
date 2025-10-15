package com.classes;

import java.io.File;
import java.util.Scanner;

import org.json.simple.ItemList;


    public class DataLoader {
        private String source;

        public DataLoader(String source) {
            this.source = source;
        }

    }

    public GameSystem loadGame(){
        GameSystem game = new GameSystem();
        game.setPlayer(loadPlayers());
        game.setPuzzles(loadPuzzles());
        game.setRooms(loadRooms());
        game.setLeaderboard(new Leaderboard());
        game.setHints(new Hints());
        game.setTimer(new Timer());
        return game;
    }
    public PlayerList loadPlayers(){
        PlayerList players = new PlayerList();
        File f = new File(source + "/players.txt");
        try (Scanner scanner = new Scanner(f)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine(). trim();
                if(line.siEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("",2); // name score 
                String name = parts[0].trim();
                int score = 0;
                if (parts.length > 1) {
                    try { score = Integer.parseInt(parts[1].trim()); }
                catch (NumberFormatException ignored) { /* leave score as 0 */ }

                    }

                    Player p = new Player(name, score);
                    players.add(p);

                }
        } catch (exception e) {
            System.out.println("Could not load players.txt ")
        }

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


