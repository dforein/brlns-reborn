package com.brlnsreb.minigames.listeners.general;

import com.brlnsreb.minigames.core.player.CustomPlayer;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerCreationEvent;

public class PlayerCreationListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(CustomPlayer.class);
    }
    
}
