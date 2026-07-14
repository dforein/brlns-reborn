package org.brlnsreb.minigames.mm.match.game.ui;

import java.util.Set;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMPlayerGameData;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

public class MMBossBar extends BossBarAbstract {

    private final Set<CustomPlayer> players;
    private static int timeStartTrackingVar;

    public MMBossBar(Set<CustomPlayer> players, int timeStartTracking) {
        this.players = players;
        timeStartTrackingVar = timeStartTracking;
    }

    public void updateGameBossBar(CustomPlayer player, MMPlayerGameData gameData, int secondsRemaining) {
        switch (gameData.role) {
            case INNOCENT -> updateExpAndGold(player, gameData);
            case SHERIFF -> updateExp(player, gameData);
            case MURDERER -> {
                if (secondsRemaining <= timeStartTrackingVar) updateExpAndDistance(player, gameData);
            }
        }
    }
    
    public void updateExpAndGold(CustomPlayer innocent, MMPlayerGameData gameData) {
        updateBossBar(innocent,
            "§l§7- §a" + gameData.getExpEarned() + " EXP §7¦ §e" + gameData.gold + " GOLD §7-"
        );
    }

    public void updateExp(CustomPlayer player, MMPlayerGameData gameData) {
        updateBossBar(player,
            "§l§7- §a" + gameData.getExpEarned() + " EXP §7-"
        );
    }

    public void updateExpAndDistance(CustomPlayer murderer, MMPlayerGameData gameData) {
        updateBossBar(murderer,
            "§l§7- §aNEAREST: " + "%.2f".formatted(getNearestDistance(murderer)) + "m §7¦ §a" + gameData.getExpEarned() + " EXP §7-"
        );
    }

    private double getNearestDistance(CustomPlayer murderer) {
        double nearest = Double.MAX_VALUE;

        for (CustomPlayer p : players) {
            double distSq = murderer.distanceSquared(p);
            if (distSq < nearest) nearest = distSq;
        }

        return Math.sqrt(nearest);
    }
}