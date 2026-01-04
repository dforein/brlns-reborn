package com.brlnsreb.minigames.core;

import cn.nukkit.Player;
import java.util.List;

public abstract class Minigame {
    
    protected String name;
    protected GameState state;
    protected Arena arena;
    protected List<Player> players;
    
    // Lifecycle
    public abstract void onGameStart();
    public abstract void onGameEnd();
    public abstract void onPlayerJoin(Player player);
    public abstract void onPlayerLeave(Player player);
    
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