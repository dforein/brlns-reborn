package org.brlnsreb.listeners.general;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerFoodLevelChangeEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerEatListener implements Listener {
    
    @EventHandler
    public void onPlayerFoodLevelChange(PlayerFoodLevelChangeEvent event) {
        event.setFoodLevel(18);
    }

}
