package org.brlnsreb.core.minigame.match;

import java.util.Set;

import org.brlnsreb.utils.Messages;

import cn.nukkit.Player;

public abstract class MinigameGame {

    protected GameState state;
    protected final Set<Player> players;
    protected final Arena arena;
    protected final Messages msgUtil;

    public MinigameGame(MinigameMatch match, String map) {
        this.players = match.getPlayers();
        this.arena = prepareArena(map, match);
        this.msgUtil = match.getMsgUtil();
        this.state = match.getState();
    }

    private Arena prepareArena(String map, MinigameMatch match) {
        return new Arena(
            match.getConfig(), 
            "map-settings.maps." + map,
            "settings."
        );
    }
    
    public abstract void onGameStart();
    public abstract void onGameEnding();
    public abstract void forceStop();
    
    public abstract boolean checkWinCondition();    //should be considered also the case where everyone left the game, so no winners

}
