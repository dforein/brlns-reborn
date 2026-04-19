package com.brlnsreb.minigames.core.minigame;

import cn.nukkit.Player;
import java.util.List;

// TODO: implement and refine this astraction

public abstract class MinigameMatch {
    
    protected String name;
    protected int id;
    protected GameState state;
    protected Arena arena;
    protected List<Player> players;
    
    // Lifecycle
    public abstract void onGameStart();
    public abstract void onGameEnd();
    public abstract void onJoin(Player player);
    public abstract void onLeave(Player player);
    public abstract void onJoinAsSpectator(Player player);
    
    // Win conditions
    public abstract boolean checkWinCondition();
    public abstract List<Player> getWinners();
    
    // Config
    public abstract int getMinPlayers();
    public abstract int getMaxPlayers();
    
    // Getters
    public String getName() { return name; }
    public int getId() { return id; }
    public GameState getState() { return state; }
}