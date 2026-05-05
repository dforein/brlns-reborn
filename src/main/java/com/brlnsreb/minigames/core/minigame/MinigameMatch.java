package com.brlnsreb.minigames.core.minigame;

import cn.nukkit.Player;
import java.util.List;

import com.brlnsreb.minigames.core.State;

// TODO: implement and refine this astraction

public abstract class MinigameMatch {
    
    protected int id;
    protected State state;
    protected Arena arena;
    protected List<Player> players;
    
    // Lifecycle
    public abstract void onJoin(Player player);
    public abstract void onJoinAsSpectator(Player player);
    public abstract void onLeave(Player player);
    public abstract void onGameStart();
    public abstract void onGameEnd();
    public abstract void forceStop();
    
    // Win conditions
    public abstract boolean checkWinCondition();
    public abstract List<Player> getWinners();
    
    // Config
    public abstract int getMinPlayers();
    public abstract int getMaxPlayers();
    
    // Getters
    public int getId() { return id; }
    public State getState() { return state; }
}