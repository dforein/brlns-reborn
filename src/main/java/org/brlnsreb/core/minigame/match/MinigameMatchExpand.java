package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;

public abstract class MinigameMatchExpand extends MinigameMatch {

    protected EndLobby endLobby;
    
    public MinigameMatchExpand(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
        this.endLobby = new EndLobby(this);
    }

    public void onDeath(CustomPlayer player) {
        players.remove(player);
        game.prepareAndSaveData(player);
        endLobby.onJoin(player);
    }

}
