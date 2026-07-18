package org.brlnsreb.listeners.general;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.ChatMsgs;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.Player;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class ChatListener implements Listener {

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        switch (player.state) {
            case PLAYING:
                if (!player.matchCurrent.getGame().onChat(player, event)) {
                    event.setCancelled();
                    return;
                }
                event.setFormat(player.ingameChatNameTag + "§7: %s");
                break;
            
            case SPECTATOR:
                event.setFormat(ChatMsgs.SPEC_PFX + player.data.name + ": %s");

            default:
                int floorLevel = player.data.getFloorLevel();
                event.setFormat((floorLevel < 1000 ? " " : "") + player.getNameTag() + "§7: %s");
                break;
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

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        switch (player.state) {
            case PLAYING, SPECTATOR:
                if (!player.matchCurrent.getGame().onCommandPreprocess(player, event)) {
                    event.setCancelled();
                    return;
                }
            
            default: break;
        }
    }

}
