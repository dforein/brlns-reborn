package com.brlnsreb.minigames.core.minigame;

import com.brlnsreb.minigames.mm.MurderMystery;

public class MinigameManager {
    
    private final Minigame[] minigames = {
        new MurderMystery(Minigames.MURDER_MYSTERY)
    };

    public Minigame getMinigame(Minigames minigame) {
        for (Minigame mg : this.minigames) {
            if (mg.getId() == minigame.getId()) return mg;
        }
        return null;
    }

}
