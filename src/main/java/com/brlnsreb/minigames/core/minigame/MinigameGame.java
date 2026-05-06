package com.brlnsreb.minigames.core.minigame;

import java.util.Set;

import com.brlnsreb.minigames.utils.MessageUtil;

import cn.nukkit.Player;

public abstract class MinigameGame {

    protected GameState state;
    protected final Set<Player> players;
    protected final Arena arena;
    protected final MessageUtil messages;

    public MinigameGame(Set<Player> players, Arena arena, GameState state, MessageUtil messages) {
        this.players = players;
        this.arena = arena;
        this.messages = messages;
        this.state = state;
    }
    
    public abstract void onGameStart();
    public abstract void onGameEnding();
    public abstract void forceStop();
    
    public abstract boolean checkWinCondition();    //should be considered also the case where everyone left the game, so no winners

}
