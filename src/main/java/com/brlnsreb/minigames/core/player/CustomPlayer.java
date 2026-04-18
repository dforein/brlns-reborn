package com.brlnsreb.minigames.core.player;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.network.connection.BedrockSession;
import cn.nukkit.network.protocol.types.PlayerInfo;

public class CustomPlayer extends Player {

    public boolean isGameSpectator = false;

    public CustomPlayer(@NotNull BedrockSession session, @NotNull PlayerInfo info) {
        super(session, info);

        //search player data in db through DatabaseManager and save in a PlayerData instance
    }

    public boolean isGameSpectator() {
        return isGameSpectator;
    }

    public void setGameSpectator(boolean value) {
        this.setGameSpectator(value, false);
    }
    
    public void setGameSpectator(boolean value, boolean spawnToAll) {
        this.isGameSpectator = value;

        if (value) {
            this.despawnFromAll();
        } else if (spawnToAll) {
            this.spawnToAll();
        }
    }

    @Override
    public void spawnTo(Player player) {
        if (this.isGameSpectator) return;
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled();
        return false;
    }

}
