package org.brlnsreb.minigames.mm.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.MinigameMatchExpand;
import org.brlnsreb.core.minigame.match.game.MinigameGame;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;

public class MMMatch extends MinigameMatchExpand {

    public MMMatch(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
    }

    protected WaitingLobby createWaitingLobby() {
        return new MMWaitingLobby(this);
    }

    protected MinigameGame createGame(String selectedMap) {
        return new MMGame(this, selectedMap);
    }

}
