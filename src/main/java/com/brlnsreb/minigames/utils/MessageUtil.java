package com.brlnsreb.minigames.utils;

import java.util.Collection;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class MessageUtil {

    private Config messages;
    private final Collection<Player> players;
    private final String prefix;
    
    public MessageUtil(Config messages, Collection<Player> players) {
        this.messages = messages;
        this.players = players;
        this.prefix = messages.getString("prefix") + " &r";
    }

    public void reloadConfig(Config messages) {
        this.messages = messages;
    }

    public void broadcastPreset(String field) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(messages.getString(field)), 
            players
        );
    }

    public void broadcastPreset(String field, Map<String, String> placeholders) {
        Server.getInstance().broadcastMessage(
            replacePlaceholders(TextFormat.colorize(messages.getString(field)), placeholders), 
            players
        );
    }

    public void broadcastPresetPrefix(String field) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(prefix + messages.getString(field)), 
            players
        );
    }

    public void broadcastPresetPrefix(String field, Map<String, String> placeholders) {
        Server.getInstance().broadcastMessage(
            replacePlaceholders(TextFormat.colorize(prefix + messages.getString(field)), placeholders), 
            players
        );
    }

    public void broadcast(String message) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(message), 
            players
        );
    }

    public void broadcast(String message, Map<String, String> placeholders) {
        Server.getInstance().broadcastMessage(
            replacePlaceholders(TextFormat.colorize(message), placeholders), 
            players
        );
    }

    public void broadcastPrefix(String message) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(prefix + message), 
            players
        );
    }

    public void broadcastPrefix(String message, Map<String, String> placeholders) {
        Server.getInstance().broadcastMessage(
            replacePlaceholders(TextFormat.colorize(prefix + message), placeholders), 
            players
        );
    }

    public void sendPresetMessage(String field, Player player) {
        player.sendMessage(TextFormat.colorize(
            messages.getString(field)
        ));
    }

    public void sendPresetMessage(String field, Player player, Map<String, String> placeholders) {
        player.sendMessage(TextFormat.colorize(
            replacePlaceholders(messages.getString(field), placeholders)
        ));
    }

    public void sendPresetMessagePrefix(String field, Player player) {
        player.sendMessage(TextFormat.colorize(
            prefix + messages.getString(field)
        ));
    }

    public void sendPresetMessagePrefix(String field, Player player, Map<String, String> placeholders) {
        player.sendMessage(TextFormat.colorize(
            replacePlaceholders(prefix + messages.getString(field), placeholders)
        ));
    }

    public void sendMessage(String message, Player player) {
        player.sendMessage(TextFormat.colorize(message));
    }

    public void sendMessage(String message, Player player, Map<String, String> placeholders) {
        player.sendMessage(TextFormat.colorize(
            replacePlaceholders(message, placeholders)
        ));
    }

    public void sendMessagePrefix(String message, Player player) {
        player.sendMessage(TextFormat.colorize(prefix + message));
    }

    public void sendMessagePrefix(String message, Player player, Map<String, String> placeholders) {
        player.sendMessage(TextFormat.colorize(
            replacePlaceholders(prefix + message, placeholders)
        ));
    }

    public String getPrefix() {
        return prefix;
    }

    public String replacePlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
    
}
