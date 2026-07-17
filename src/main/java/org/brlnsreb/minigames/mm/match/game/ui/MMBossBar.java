package org.brlnsreb.minigames.mm.match.game.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

public class MMBossBar extends BossBarAbstract {

    private final MMGame game;
    private static int timeStartTracking;

    public MMBossBar(MMGame game) {
        this.game = game;
        timeStartTracking = game.getConfig().getInt("game.time-start-tracking");
    }

    public void updateGameBossBars() {
        for (CustomPlayer p : game.getPlayers()) {
            updateGameBossBar(p);
        }
    }

    public void updateGameBossBar(CustomPlayer player) {
        MMPlayerGameData gameData = game.getGameData(player);

        switch (gameData.role) {
            case INNOCENT -> updateExpAndGold(player, gameData);
            case SHERIFF -> updateExp(player, gameData);
            case MURDERER -> {
                if (game.getTimer().getSecondsRemaining() <= timeStartTracking) updateExpAndDistance(player, gameData);
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

        for (CustomPlayer p : game.getPlayers()) {
            double distSq = murderer.distanceSquared(p);
            if (distSq < nearest) nearest = distSq;
        }

        return Math.sqrt(nearest);
    }
}