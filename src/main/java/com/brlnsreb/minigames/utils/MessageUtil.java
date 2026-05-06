package com.brlnsreb.minigames.utils;

import java.util.Collection;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class MessageUtil {

    private final Config messages;
    private final Collection<Player> players;
    private final String prefix;
    
    public MessageUtil(Config messages, Collection<Player> players) {
        this.messages = messages;
        this.players = players;
        this.prefix = messages.getString("prefix") + " &r";
    }

    public void broadcastPreset(String field) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(messages.getString(field)), 
            players
        );
    }

    public void broadcastPresetPrefix(String field) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(prefix + messages.getString(field)), 
            players
        );
    }

    public void broadcast(String message) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(message), 
            players
        );
    }

    public void broadcastPrefix(String message) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(prefix + message), 
            players
        );
    }

    public void sendPresetMessage(String field, Player player) {
        player.sendMessage(
            TextFormat.colorize(messages.getString(field))
        );
    }

    public void sendPresetMessagePrefix(String field, Player player) {
        player.sendMessage(
            TextFormat.colorize(prefix + messages.getString(field))
        );
    }

    public void sendMessage(String message, Player player) {
        player.sendMessage(TextFormat.colorize(message));
    }

    public void sendMessagePrefix(String message, Player player) {
        player.sendMessage(TextFormat.colorize(prefix + message));
    }

    public String getPrefix() {
        return prefix;
    }
    
}
