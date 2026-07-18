package org.brlnsreb.listeners.general;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.database.AccountsManager;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerKickEvent;
import org.powernukkitx.event.player.PlayerQuitEvent;
import org.powernukkitx.plugin.annotation.EventListener;

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
        GeneralLobby.onlinePlayers--;
        CustomPlayer player = (CustomPlayer) p;

        AccountsManager.savePlayerData(player);
        FriendsManager.removeOnlineFriend(player.data);
        Match match  = player.getMatch();
        if (match != null) match.onLeave(player);
        player.save();
    }
    
}