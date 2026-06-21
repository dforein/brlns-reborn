package org.brlnsreb.core.player;

import java.util.UUID;

import cn.nukkit.Server;

public abstract class PlayerGameData {
    
    public final UUID uuid;
    public final PlayerData playerData;
    //private int expEarned;

    public PlayerGameData(CustomPlayer player) {
        this.uuid = player.getUniqueId();
        this.playerData = player.getPlayerData();
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

}
