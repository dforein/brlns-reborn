package com.brlnsreb.minigames.core.minigame;

import java.util.Set;

import com.brlnsreb.minigames.core.minigame.match.MinigameMatch;
import com.brlnsreb.minigames.mm.MurderMystery;

public class MinigameManager {
    
    private static Set<? extends Minigame> minigames;

    public MinigameManager() {
        minigames = Set.of(
            new MurderMystery(MinigameType.MURDER_MYSTERY)
        );
    }

    public static void forceStop() {
        for (Minigame mg : minigames) {
            for (MinigameMatch match : mg.getMatches()) {
                match.getGame().forceStop();
            }
        }
    }

    public static Minigame getMinigame(String gameNameTag) {
        return getMinigame(MinigameType.fromNameTag(gameNameTag));
    }

    public static Minigame getMinigame(MinigameType minigame) {
        for (Minigame mg : minigames) {
            if (mg.getId() == minigame.getId()) return mg;
        }
        return null;
    }

    public static void reloadConfig() {
        for (Minigame mg : minigames) {
            mg.reloadConfig();
        }
    }

}
