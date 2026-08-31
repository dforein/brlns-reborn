package org.brlnsreb.listeners.general;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerCreationEvent;
import org.powernukkitx.event.player.PlayerJoinEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerCreationListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(CustomPlayer.class);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");

        PlayerUtils.updateOnlinePlayer(event.getPlayer(), true); //remove the name for players who aren't in the same level (main hub)
    }

}