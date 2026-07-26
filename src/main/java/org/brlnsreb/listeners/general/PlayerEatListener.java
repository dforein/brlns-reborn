package org.brlnsreb.listeners.general;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerFoodLevelChangeEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerEatListener implements Listener {
    
    @EventHandler
    public void onPlayerFoodLevelChange(PlayerFoodLevelChangeEvent event) {
        event.setCancelled();
        PlayerUtils.setFood((CustomPlayer) event.getPlayer(), 18);
    }

}
