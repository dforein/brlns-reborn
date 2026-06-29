package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;

public abstract class MinigameMatchExpand extends MinigameMatch {

    protected EndLobby endLobby;
    
    public MinigameMatchExpand(MinigameType minigame, int matchNumber) {
        super(minigame, matchNumber);
        this.endLobby = createEndLobby("end-lobby");
    }

    public void onDeath(CustomPlayer player) {
        players.remove(player);
        game.prepareAndSaveData(player);
        endLobby.onJoin(player);
    }

    protected abstract EndLobby createEndLobby(String configPath);

}
