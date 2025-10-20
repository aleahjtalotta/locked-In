package com.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    
    private List<ScoreEntry> scores;

    public Leaderboard() {
        scores = new ArrayList<>();
    }

    public void updateLeaderboard(Player player, int score) {

        ScoreEntry found = null;
        for (ScoreEntry entry : scores) {
            if (entry.getPlayerName().equals(player.getName())) {
                found = entry;
                break;
                }
            }
            if (found != null) {
                if (score > found.getScore()) {
                    scores.remove(found);
                    scores.add(new ScoreEntry(player.getName(), score, null));

                }
            }else {
                scores.add(new ScoreEntry(player.getName(), score, null));

            }
            
            sortLeaderboard();
        }
            public void displayTopPlayer() {
                if (scores.isEmpty()) {
                    System.out.println("No scores available yet!");
                    return;
                }
                ScoreEntry top = scores.get(0);
                System.out.println("TOP PLAYER: " + top.getPlayerName() + " - " + top.getScore());
            }
            public void displayPlayerScore(Player player) {
                for (ScoreEntry entry : scores) {
                    if (entry.getPlayerName().equals(player.getName())) {
                        System.out.println(player.getName() + "'s score: " + entry.getScore());
                        return;
                    }
                }
                System.out.println(player.getName() + " has no score on the leaderboard.");
            } 
            private void sortLeaderboard() {
                Collections.sort(scores, Comparator.comparingInt(ScoreEntry::getScore).reversed());
            }
        }