package com.brlnsreb.minigames.listeners.general;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.Player;
import cn.nukkit.level.Level;

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
