package org.brlnsreb.core.minigame;

import java.util.ArrayList;
import java.util.List;

import org.brlnsreb.core.minigame.match.Match;
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
            for (Match match : new ArrayList<>(mg.getMatches())) {
                match.forceStop();
            }
        }
    }

    public static Minigame getMinigame(String gameNameTag) {
        return getMinigame(MinigameType.fromNameTag(gameNameTag));
    }

    public static Minigame getMinigame(MinigameType minigame) {
        for (Minigame mg : minigames) {
            if (mg.mgt.id == minigame.id) return mg;
        }
        return null;
    }

    public static List<? extends Minigame> getMinigames() {
        return minigames;
    }

    public static void onConfigReload() {
        for (Minigame mg : minigames) {
            mg.onConfigReload();
        }
    }

}
