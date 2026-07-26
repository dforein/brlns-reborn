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

        CustomPlayer player = (CustomPlayer) event.getPlayer();
        player.updateExp();
        player.updatePresetNameTags();
        if (player.data.isLogged()) player.setDisplayName(player.data.name);
        PlayerUtils.updateOnlinePlayer(player, true);
    }

}