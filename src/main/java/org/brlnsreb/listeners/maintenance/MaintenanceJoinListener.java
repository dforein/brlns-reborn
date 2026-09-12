package org.brlnsreb.listeners.maintenance;

import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerJoinEvent;

public class MaintenanceJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(ChatMsgs.BROKENLENS_PFX + "Maintenance mode enabled, all levels loaded. Check the console for suggestions.");
        event.getPlayer().sendMessage(ChatMsgs.BROKENLENS_PFX + "Use §e/world tp§r §dto teleport to other levels!");
    }

}
