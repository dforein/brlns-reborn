package org.brlnsreb.listeners.general;

import org.brlnsreb.core.lobby.entities.HologramEntity;

import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.EntityHuman;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.level.ChunkUnloadEvent;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class EntityChunkListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        IChunk chunk = event.getChunk();
        for(Entity entity : chunk.getEntities().values()) {
            if (entity instanceof EntityHuman || entity instanceof HologramEntity) {    //includes NPCEntity, which heredits EntityHuman
                event.setCancelled();
                return;
            }
        }
    }

}