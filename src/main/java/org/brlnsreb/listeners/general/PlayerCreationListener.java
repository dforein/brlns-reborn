package org.brlnsreb.listeners.general;

import org.brlnsreb.core.player.CustomPlayer;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerCreationEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerCreationListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(CustomPlayer.class);
    }

}