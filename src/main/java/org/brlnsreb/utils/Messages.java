package org.brlnsreb.utils;

import java.util.Collection;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class Messages {

    private Config messages;
    private final Collection<Player> players;
    private final String prefix;
    
    public Messages(Config messages, Collection<Player> players) {
        this.messages = messages;
        this.players = players;
        this.prefix = messages.getString("prefix") + " &r";
    }

    public static String getStrPrefix(String path, Config messages, String prefix) {
        return TextFormat.colorize(prefix) + YamlUtil.getStr(path, messages);
    }

    private String getString(String path, String prefix) {
        return getStrPrefix(path, this.messages, prefix);
    }

    private String getString(String path) {
        return YamlUtil.getStr(path, this.messages);
    }

    public String replacePlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return TextFormat.colorize(message);
    }


    public void broadcastPreset(String path) {
        Server.getInstance().broadcastMessage(
            getString(path), 
            players
        );
    }

    public void broadcastPreset(String path, Map<String, String> placeholders) {
        Server.getInstance().broadcastMessage(
            replacePlaceholders(getString(path), placeholders), 
            players
        );
    }

    public void broadcastPresetPrefix(String path) {
        Server.getInstance().broadcastMessage(
            getString(path, prefix), 
            players
        );
    }

    public void broadcastPresetPrefix(String path, Map<String, String> placeholders) {
        Server.getInstance().broadcastMessage(
            replacePlaceholders(getString(path, prefix), placeholders), 
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
            replacePlaceholders(message, placeholders), 
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
            replacePlaceholders(prefix + message, placeholders), 
            players
        );
    }


    public void sendPresetMessage(String path, Player player) {
        player.sendMessage(getString(path));
    }

    public void sendPresetMessage(String path, Player player, Map<String, String> placeholders) {
        player.sendMessage(
            replacePlaceholders(messages.getString(path), placeholders)
        );
    }

    public void sendPresetMessagePrefix(String path, Player player) {
        player.sendMessage(getString(path, prefix));
    }

    public void sendPresetMessagePrefix(String path, Player player, Map<String, String> placeholders) {
        player.sendMessage(
            replacePlaceholders(getString(path, prefix), placeholders)
        );
    }


    public void sendMessage(String message, Player player) {
        player.sendMessage(TextFormat.colorize(message));
    }

    public void sendMessage(String message, Player player, Map<String, String> placeholders) {
        player.sendMessage(
            replacePlaceholders(message, placeholders)
        );
    }

    public void sendMessagePrefix(String message, Player player) {
        player.sendMessage(TextFormat.colorize(prefix + message));
    }

    public void sendMessagePrefix(String message, Player player, Map<String, String> placeholders) {
        player.sendMessage(
            replacePlaceholders(prefix + message, placeholders)
        );
    }

    public String getPrefix() {
        return prefix;
    }
    
}
