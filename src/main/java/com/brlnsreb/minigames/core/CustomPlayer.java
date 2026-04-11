package com.brlnsreb.minigames.core;

import java.util.ArrayList;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.network.connection.BedrockSession;
import cn.nukkit.network.protocol.types.PlayerInfo;

public class CustomPlayer extends Player {

    public boolean isSpectator = false;

    public CustomPlayer(@NotNull BedrockSession session, @NotNull PlayerInfo info) {
        super(session, info);
    }

    @Override
    public void spawnTo(Player player) {
        if (this.isSpectator) {
            return;
        }
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled();
        return false;
    }

    @Override
    protected void checkChunks() {
        if (this.chunk == null || (this.chunk.getX() != ((int) this.x >> 4) || this.chunk.getZ() != ((int) this.z >> 4))) {
            if (this.chunk != null) {
                this.chunk.removeEntity(this);
            }
            this.chunk = this.level.getChunk((int) this.x >> 4, (int) this.z >> 4, true);

            if (!this.justCreated) {
                Map<Integer, Player> newChunk = this.level.getChunkPlayers((int) this.x >> 4, (int) this.z >> 4);
                newChunk.remove(this.getLoaderId());

                for (Player player : new ArrayList<>(this.hasSpawned.values())) {
                    if (!newChunk.containsKey(player.getLoaderId())) {
                        this.despawnFrom(player);
                    } else {
                        newChunk.remove(player.getLoaderId());
                    }
                }

                if (!isSpectator) {
                    for (Player player : newChunk.values()) {
                        this.spawnTo(player);
                    }
                }
            }

            if (this.chunk == null) {
                return;
            }

            this.chunk.addEntity(this);
        }
    }

}
