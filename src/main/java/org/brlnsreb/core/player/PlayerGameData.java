package org.brlnsreb.core.player;

import java.util.UUID;

import org.brlnsreb.core.database.StatType;

import cn.nukkit.Server;

public abstract class PlayerGameData {
    
    public final UUID uuid;
    public final PlayerData playerData;
    public final int minigameId;
    //private int expEarned;

    public PlayerGameData(CustomPlayer player) {
        this.uuid = player.getUniqueId();
        this.playerData = player.getPlayerData();
        this.minigameId = player.currentMinigame.getId();
    }

    public CustomPlayer getPlayer() {
        return (CustomPlayer) Server.getInstance().getPlayer(uuid).orElse(null);
    }

    public String getName() {
        return playerData.name;
    }

    public void addExp(int deltaExp) {
        playerData.addExp(deltaExp);
    }

    public void addCoins(int deltaCoins) {
        playerData.addCoins(deltaCoins);
    }

    public void incrementGlobalStat(StatType statType) {
        playerData.incrementGlobalStat(statType);
    }

    public void incrementStat(StatType statType) {
        playerData.incrementStat(minigameId, statType);
    }

}
