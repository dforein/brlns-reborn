package org.brlnsreb.core.minigame.game;

import java.util.Set;

import org.brlnsreb.core.minigame.match.GameState;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.Messages;

public abstract class MinigameGame {

    protected final MinigameMatch match;
    protected final GameState state;
    protected final Set<CustomPlayer> players;
    protected final Arena arena;
    protected final Messages msgUtil;

    public MinigameGame(MinigameMatch match, String map) {
        this.match = match;
        this.state = match.getState();
        this.players = match.getPlayers();
        this.arena = prepareArena(map, match);
        this.msgUtil = match.getMsgUtil();
    }

    private Arena prepareArena(String map, MinigameMatch match) {
        return new Arena(
            match.getConfig(), 
            "map-settings.maps." + map,
            "settings."
        );
    }

    public abstract void onLeave(CustomPlayer player);

    public void close() {
        arena.close();
    }
    
    public abstract void onGameStart();
    public abstract void onGameEnding();
    public abstract void forceStop();
    
    public abstract boolean checkWinCondition();    //should be considered also the case where everyone left the game, so no winners

}
