package com.brlnsreb.minigames.listeners.general;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockUpdateEvent;

public class BlockUpdateListener implements Listener {

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        event.setCancelled(true);
    }

}
