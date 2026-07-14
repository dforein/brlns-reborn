package org.brlnsreb.core.player.data;

import java.util.UUID;

import org.brlnsreb.core.player.CustomPlayer;

public abstract class PlayerGameData {
    
    public final UUID uuid;
    public final PlayerData playerData;
    public final int minigameId;
    protected int expEarned = 0;
    protected int coinsEarned = 0;

    public PlayerGameData(CustomPlayer player) {
        this.uuid = player.getUniqueId();
        this.playerData = player.data;
        this.minigameId = player.currentMinigame.mgt.id;
    }

    public void addExp(int deltaExp) {
        playerData.addExp(deltaExp);
        expEarned += deltaExp;
    }

    public void addCoins(int deltaCoins) {
        playerData.addCoins(deltaCoins);
        coinsEarned += deltaCoins;
    }

    public void incrementGlobalStat(StatType statType) {
        playerData.incrementGlobalStat(statType);
    }

    public void incrementStat(StatType statType) {
        playerData.incrementStat(minigameId, statType);
    }

    public int getExpEarned() { return expEarned; }
    public int getCoinsEarned() { return coinsEarned; }

}
