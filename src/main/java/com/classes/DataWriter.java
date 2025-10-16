package com.classes;

import java.io.FileWriter;

public class DataWriter {
    private String destination;

    public DataWriter(String destination) {
        this.destination = destination;
    }

    public void savePlayers(PlayerList pl){
        

     }

     public void saveLeaders(Leaderboard lb){
        try(FileWriter writer = new FileWriter(destination + "leaderboard.txt", false)){
            for(int i = 0; i < lb.getSize(); i++){
                writer.write(lb.getEntry(i).getName() + "," + lb.getEntry(i).getScore() + "\n");
            }
        } catch (Exception e){
            e.printStackTrace();
     }

     public void saveGame(GameSystem gs){

     }
}
