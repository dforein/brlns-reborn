package com.brlnsreb.minigames.listeners.general;

import com.brlnsreb.minigames.MinigameCore;

import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.Player;
import org.powernukkitx.level.Level;

public class ChatListener implements Listener{

    private final MinigameCore plugin;

    public ChatListener(MinigameCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onChat(PlayerChatEvent event) {
        //avoid players chatting in different worlds

        if (plugin.getGlobalChat()) return;

        Player sender = event.getPlayer();
        Level senderLevel = sender.getLevel();

        event.getRecipients().removeIf(recipient -> 
            (recipient instanceof Player) &&
            !((Player) recipient).getLevel().equals(senderLevel)
        );
    }
}
