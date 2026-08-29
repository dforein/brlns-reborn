package org.brlnsreb.listeners.general;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.DeathLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.database.AccountsManager;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.utils.abstraction.ScoreboardAbstract;
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerKickEvent;
import org.powernukkitx.event.player.PlayerQuitEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerQuitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        handlePlayerLeave(event.getPlayer());
        event.setQuitMessage("");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        handlePlayerLeave(event.getPlayer());
        event.setQuitMessage("");
    }

    private void handlePlayerLeave(Player p) {
        BrlnsReb.logger.info("Player " + p.getDisplayName() + " logout registered successfully");

        PlayerUtils.onlinePlayers--;
        CustomPlayer player = (CustomPlayer) p;

        AccountsManager.savePlayerData(player);
        FriendsManager.removeOnlineFriend(player.data);
        if (player.matchCurrent != null) {
            player.matchCurrent.onLeave(player);
        }
        if (player.getLobby() != null && player.getLobby() instanceof DeathLobby deathLobby) {
            deathLobby.onLeave(p);
        }
        ScoreboardAbstract.remove(player);
        PlayerDataManager.onServerLeave(player);
        player.save();
    }
    
}