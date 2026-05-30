package com.brlnsreb.minigames.core.minigame.match;

import java.util.Set;

import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.utils.MessageUtil;

import cn.nukkit.Player;

public abstract class MinigameGame {

    protected GameState state;
    protected final Set<Player> players;
    protected final Arena arena;
    protected final MessageUtil msgUtil;

    public MinigameGame(Set<Player> players, Arena arena, GameState state, MessageUtil msgUtil) {
        this.players = players;
        this.arena = arena;
        this.msgUtil = msgUtil;
        this.state = state;
    }
    
    public abstract void onGameStart();
    public abstract void onGameEnding();
    public abstract void forceStop();
    
    public abstract boolean checkWinCondition();    //should be considered also the case where everyone left the game, so no winners

    public void onDeath(CustomPlayer player) {}        //override this if player death is allowed

}
