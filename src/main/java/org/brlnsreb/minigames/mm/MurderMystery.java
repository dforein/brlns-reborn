package org.brlnsreb.minigames.mm;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameLobby;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.minigames.mm.match.MMMatch;

public class MurderMystery extends Minigame {
    
    public MurderMystery(MinigameType minigame) {
        super(minigame);
    }

    protected MinigameLobby createLobby() {
        return new MMLobby(this);
    }

    protected MinigameMatch createMatch(int newMatchNumber) {
        return new MMMatch(this, newMatchNumber);
    }
}
