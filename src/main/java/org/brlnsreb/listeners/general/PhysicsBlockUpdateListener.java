package org.brlnsreb.listeners.general;

import org.brlnsreb.core.WorldManager;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockUpdateEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PhysicsBlockUpdateListener implements Listener {

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        //by default the physics are disabled; 
        //if the levelName is inside enabledLevels, the physics are enabled

        if (WorldManager.getEnabledPhysicsLevels().contains(
            event.getBlock().getLevel().getId()
        )) return;
        
        event.setCancelled(true);
    }

}
