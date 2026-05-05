package com.brlnsreb.minigames.listeners.general;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.level.format.IChunk;

public class EntityChunkListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        IChunk chunk = event.getChunk();
        for(Entity entity : chunk.getEntities().values()) {
            if(entity instanceof EntityHuman) {
                event.setCancelled();
                return;
            }
        }
    }

}