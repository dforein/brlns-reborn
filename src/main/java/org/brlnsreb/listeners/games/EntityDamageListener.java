package org.brlnsreb.listeners.games;

import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class EntityDamageListener implements Listener {
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof CustomPlayer player && player.isPlaying()) {
            player.matchCurrent.getGame().onPlayerDamage(player, event);
        }
    }

}
