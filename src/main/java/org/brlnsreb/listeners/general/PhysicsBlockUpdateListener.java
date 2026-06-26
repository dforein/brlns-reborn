package org.brlnsreb.listeners.general;

import org.brlnsreb.core.WorldManager;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockUpdateEvent;
import cn.nukkit.plugin.annotation.EventListener;

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
