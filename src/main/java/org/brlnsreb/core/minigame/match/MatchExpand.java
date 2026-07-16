package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.player.CustomPlayer;

public abstract class MatchExpand extends Match {

    protected EndLobby endLobby;
    
    public MatchExpand(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
        this.endLobby = new EndLobby(this);
    }

    public void onEndLobbyJoin(CustomPlayer player) {
        players.remove(player);
        endLobby.onJoin(player);
    }

    @Override
    public GameExpand getGame() { return (GameExpand) game; }

}
