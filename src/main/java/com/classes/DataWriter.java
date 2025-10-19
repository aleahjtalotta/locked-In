package com.classes;
import java.io.FileWriter;
import java.io.IOException;

public class DataWriter {
    private String destination;

    public DataWriter(String destination) {
        this.destination = destination;
    }

    public void savePlayers(PlayerList pl){
        try (FileWriter writer = new FileWriter(destination + "players.txt", false)) {
            for (int i = 0; i < pl.getSize(); i++) {
                Player p = pl.getPlayer(i);
                writer.write(p.getName() + "," + p.getEmail() + "," + p.getCurrentScore() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

     }

     public void saveLeaders(Leaderboard lb){
        try(FileWriter writer = new FileWriter(destination + "leaderboard.txt", false)){
            for(int i = 0; i < lb.getSize(); i++){
               ScoreEntry entry = lb.getEntry(i);
                writer.write(entry.getPlayerName() + "," + entry.getScore() + "\n");
            }
        } catch (Exception e){
            e.printStackTrace();
     }

     public void saveGame(GameSystem gs){
        try (FileWriter writer = new FileWriter(destination + "game.txt", false)) {
            writer.write("Difficulty: " + gs.getDifficulty() + "\n");
            writer.write("Number of Players:" + gs.getPlayerList().getSize() + "\n");
            writer.write("Number of Puzzles: " + gs.getPuzzleList().getSize() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
}
     }