package org.brlnsreb.core.minigame;

import java.util.List;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.minigames.mm.MurderMystery;

public class MinigameManager {
    
    private static List<? extends Minigame> minigames;

    public MinigameManager() {
        minigames = List.of(
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

    public static List<? extends Minigame> getMinigames() {
        return minigames;
    }

    public static void reloadConfig() {
        for (Minigame mg : minigames) {
            mg.reloadConfig();
        }
    }

}
