package com.brlnsreb.minigames.core.minigame;

import com.brlnsreb.minigames.mm.MurderMystery;

public class MinigameManager {
    
    private final Minigame[] minigames = {
        new MurderMystery(MinigameType.MURDER_MYSTERY)
    };

    public Minigame getMinigame(MinigameType minigame) {
        for (Minigame mg : this.minigames) {
            if (mg.getId() == minigame.getId()) return mg;
        }
        return null;
    }

}
