package org.brlnsreb.listeners.general;

import org.brlnsreb.BrlnsReb;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.annotation.EventListener;

@EventListener
public class ChatListener implements Listener {

    @EventHandler
    private void onChat(PlayerChatEvent event) {
        //avoid players chatting in different worlds
        if (BrlnsReb.getGlobalChat()) return;

        Player sender = event.getPlayer();
        Level senderLevel = sender.getLevel();

        event.getRecipients().removeIf(recipient -> 
            (recipient instanceof Player) &&
            !((Player) recipient).getLevel().equals(senderLevel)
        );
    }

}
