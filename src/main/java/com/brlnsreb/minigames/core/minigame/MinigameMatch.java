package com.brlnsreb.minigames.core.minigame;

import cn.nukkit.Player;
import java.util.List;

// TODO: implement and refine this astraction

public abstract class MinigameMatch {
    
    protected String name;
    protected GameState state;
    protected Arena arena;
    protected List<Player> players;
    
    // Lifecycle
    public abstract void onGameStart();
    public abstract void onGameEnd();
    public abstract void joinPlayer(Player player);
    public abstract void leavePlayer(Player player);
    
    // Win conditions
    public abstract boolean checkWinCondition();
    public abstract List<Player> getWinners();
    
    // Config
    public abstract int getMinPlayers();
    public abstract int getMaxPlayers();
    
    // Getters
    public String getName() { return name; }
    public GameState getState() { return state; }
}