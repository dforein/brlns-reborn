package org.brlnsreb.listeners.games;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class EntityDamageListener implements Listener {
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof CustomPlayer) {
            CustomPlayer player = (CustomPlayer) entity;
            
            if (player.state == PlayerStateType.PLAYING) {
                player.getMatch().getGame().onPlayerDamage(player, event);
            }
        }
    }

}
