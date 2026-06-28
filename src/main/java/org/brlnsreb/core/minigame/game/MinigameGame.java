package org.brlnsreb.core.minigame.game;

import java.util.Set;

import org.brlnsreb.core.minigame.match.GameState;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.utils.Messages;

import cn.nukkit.utils.Config;

public abstract class MinigameGame {

    protected final MinigameMatch match;
    protected final GameState state;
    protected final Set<CustomPlayer> players;
    protected final Arena arena;

    protected final Config config;
    protected final Messages msgUtil;

    public MinigameGame(MinigameMatch match, String map) {
        this.match = match;
        this.state = match.getState();
        this.players = match.getPlayers();
        this.arena = prepareArena(map, match);

        this.config = match.getConfig();
        this.msgUtil = match.getMsgUtil();
    }

    private Arena prepareArena(String map, MinigameMatch match) {
        return new Arena(
            match.getConfig(), 
            "map-settings.maps." + map,
            "settings."
        );
    }

    
    //join-leave logic

    public void onJoin(CustomPlayer player) {
        player.state = PlayerStateType.TELEPORTING;
        onJoinTeleport(player);

        player.state = PlayerStateType.PLAYING;
    }

    protected abstract void onJoinTeleport(CustomPlayer player);
    protected abstract void preparePlayer(CustomPlayer player);

    public abstract void onLeave(CustomPlayer player);


    //game lifecycle

    public void close() {
        arena.close();
    }
    
    public void onGameStart() {
        
    }

    public abstract void onGameEnding();
    public abstract void forceStop();
    
    public abstract boolean checkWinCondition();    //should be considered also the case where everyone left the game, so no winners

}
