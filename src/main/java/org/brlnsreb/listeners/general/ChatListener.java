package org.brlnsreb.listeners.general;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class ChatListener implements Listener {

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (player.state == PlayerStateType.PLAYING) {
            if (!player.getMatch().getGame().onChat(player, event)) {
                event.setCancelled();
                return;
            }
            event.setFormat(player.ingameChatName + "§7: %s");
        } else {
            int floorLevel = player.getPlayerData().getFloorLevel();
            event.setFormat((floorLevel < 1000 ? " " : "") + player.getNameTag() + "§7: %s");
        }

        filterPlayerBySenderLevel(event);
    }

    private void filterPlayerBySenderLevel(PlayerChatEvent event) {
        //avoid players chatting in different worlds
        if (BrlnsReb.getGlobalChat()) return;

        Level senderLevel = event.getPlayer().getLevel();
        event.getRecipients().removeIf(recipient -> 
            (recipient instanceof Player) &&
            !((Player) recipient).getLevel().equals(senderLevel)
        );
    }

}
