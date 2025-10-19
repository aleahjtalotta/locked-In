package com.classes;

public class GameFacade {
    private GameSystem gameSystem;
    private DataLoader dataLoader;
    private DataWriter dataWriter;
    private Player currentPlayer;

    public GameFacade(String dataSource) {
        this.dataLoader = new DataLoader(dataSource);
        this.dataWriter = new DataWriter(dataSource);
        this.gameSystem = new GameSystem();
    }


public void startNewGame() {
    this.gameSystem = new GameSystem();
    this.currentPlayer = null;
    if (gameSystem.getPlayer() == null) gameSystem.setPlayers(new PlayerList());
    if (gameSystem.getPuzzles() == null) gameSystem.setPuzzles(new PuzzleList());
        if (gameSystem.getRooms() == null) gameSystem.setRooms(new RoomList());
        if (gameSystem.getLeaderboard() == null) gameSystem.setLeaderboard(new Leaderboard());
        if (gameSystem.getHints() == null) gameSystem.setHints(new Hints());
        if (gameSystem.getTimer() == null) gameSystem.setTimer(new Timer());
}

public boolean loadGame() {
    try {
        GameSystem loader = dataLoader.loadGame();
        if(loaded != null) {
            this.gameSystem = loader;
            this.currentPlayer = null;
            return true;
        }
        return false;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
    }

public boolean saveGame() {
    try {
        dataWriter.saveGame(this.gameSystem);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;

    }
    }
}

public boolean submitAnswer() {
    //
}

public void showLeaderboard() {
    //
}

public void loginPlayer() {
    //
}

public void logoutPlayer() {
    //
}



