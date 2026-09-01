package org.brlnsreb.listeners.games;

import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class ProjectileHitListener implements Listener {
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getMovingObjectPosition().entityHit instanceof CustomPlayer playerHit)) return;
        if (!playerHit.isPlaying()) return;

        playerHit.matchCurrent.getListenerAccess().onProjectileHit(playerHit, event);
    }

}
