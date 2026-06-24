package org.brlnsreb.listeners.general;

import org.brlnsreb.core.lobby.entities.HologramEntity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class EntityChunkListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        IChunk chunk = event.getChunk();
        for(Entity entity : chunk.getEntities().values()) {
            if(entity instanceof EntityHuman || entity instanceof HologramEntity) {
                event.setCancelled();
                return;
            }
        }
    }

}