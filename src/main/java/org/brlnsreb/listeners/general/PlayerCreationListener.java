package org.brlnsreb.listeners.general;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerCreationEvent;
import org.powernukkitx.event.player.PlayerLoginEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerCreationListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(CustomPlayer.class);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (BrlnsReb.isUnderMaintenance()) {
            event.getPlayer().kick("Sorry, the server is under maintenance!");
        }
    }

}