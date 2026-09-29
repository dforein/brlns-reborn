package org.brlnsreb.listeners.games;

import org.brlnsreb.core.player.CustomPlayer;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockPlaceEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class BlockPlaceListener implements Listener {
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        CustomPlayer.putPlacedBlock(event.getBlock());
    }

}
