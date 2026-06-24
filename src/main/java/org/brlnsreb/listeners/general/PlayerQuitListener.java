package org.brlnsreb.listeners.general;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerDataManager;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class PlayerQuitListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        handlePlayerLeave(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        handlePlayerLeave(event.getPlayer());
    }


    private void handlePlayerLeave(Player p) {
        CustomPlayer player = (CustomPlayer) p;

        PlayerDataManager.savePlayerData(player.getUniqueId());
        player.getMatch().onLeave(player);
        player.save();
    }
    
}