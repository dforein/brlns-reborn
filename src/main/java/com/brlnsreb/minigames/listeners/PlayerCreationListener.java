package com.brlnsreb.minigames.listeners;

import com.brlnsreb.minigames.core.player.CustomPlayer;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCreationEvent;

public class PlayerCreationListener implements Listener {
    //TODO: add to listeners in core

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(CustomPlayer.class);
    }
    
}
