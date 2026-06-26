package org.brlnsreb.listeners.general;

import org.brlnsreb.core.player.CustomPlayer;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class BlockPlaceListener implements Listener {
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        CustomPlayer.putPlacedBlock(event.getBlock());
    }

}
