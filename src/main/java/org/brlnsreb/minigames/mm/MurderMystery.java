package org.brlnsreb.minigames.mm;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.minigames.mm.match.MMMatch;

public class MurderMystery extends Minigame {
    
    public MurderMystery(MinigameType mgt) {
        super(mgt);
    }

    protected Match createMatch(int newMatchNumber) {
        return new MMMatch(this, newMatchNumber);
    }
}
