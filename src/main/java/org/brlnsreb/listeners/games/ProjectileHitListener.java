package org.brlnsreb.listeners.games;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class ProjectileHitListener implements Listener {
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity entityHit = event.getMovingObjectPosition().entityHit;
        if (!(entityHit instanceof CustomPlayer)) return;

        CustomPlayer playerHit = (CustomPlayer) entityHit;
        if (playerHit.state != PlayerStateType.PLAYING) return;

        playerHit.getMatch().getGame().onProjectileHit(playerHit, event);
    }

}
