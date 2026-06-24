package org.brlnsreb.listeners.general;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockUpdateEvent;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class BlockUpdateListener implements Listener {

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        event.setCancelled(true);
    }

}
