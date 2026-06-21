package org.brlnsreb.listeners.general;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockUpdateEvent;

public class BlockUpdateListener implements Listener {

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        event.setCancelled(true);
    }

}
