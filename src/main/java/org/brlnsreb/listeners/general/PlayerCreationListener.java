package org.brlnsreb.listeners.general;

import org.brlnsreb.core.player.CustomPlayer;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCreationEvent;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class PlayerCreationListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(CustomPlayer.class);
    }

}