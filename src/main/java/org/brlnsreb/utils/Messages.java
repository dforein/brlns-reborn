package org.brlnsreb.utils;

import java.util.Collection;
import java.util.Set;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class Messages {

    private Config messages;
    private final Collection<? extends Player> players;
    private final String prefix;
    
    public Messages(Config messages, Collection<? extends Player> players) {
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


    public static void sendActionBar(Set<? extends Player> players, String path, Config messages) {
        for (Player p : players) {
            p.sendActionBar(YamlUtil.getStr(path, messages));
        }
    }

    public static void sendActionBar(Set<? extends Player> players, String path, Object[] placeholders, Config messages) {
        for (Player p : players) {
            p.sendActionBar(YamlUtil.getStr(path, messages).formatted(placeholders));
        }
    }


    public void sendTitle(String pathTitle, String pathSubTitle) {
        for (Player p : players) {
            p.sendTitle(getString(pathTitle), getString(pathSubTitle));
        }
    }


    public void broadcastPreset(String path) {
        Server.getInstance().broadcastMessage(
            getString(path), 
            players
        );
    }

    public void broadcastPreset(String path, Object[] placeholders) {
        Server.getInstance().broadcastMessage(
            getString(path).formatted(placeholders), 
            players
        );
    }

    public void broadcastPresetPrefix(String path) {
        Server.getInstance().broadcastMessage(
            getString(path, prefix), 
            players
        );
    }

    public void broadcastPresetPrefix(String path, Object[] placeholders) {
        Server.getInstance().broadcastMessage(
            getString(path, prefix).formatted(placeholders), 
            players
        );
    }


    public void broadcast(String message) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(message), 
            players
        );
    }

    public void broadcast(String message, Object[] placeholders) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(message.formatted(placeholders)), 
            players
        );
    }

    public void broadcastPrefix(String message) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(prefix + message), 
            players
        );
    }

    public void broadcastPrefix(String message, Object[] placeholders) {
        Server.getInstance().broadcastMessage(
            TextFormat.colorize(prefix + message.formatted(placeholders)), 
            players
        );
    }


    public void sendPresetMessage(String path, Player player) {
        player.sendMessage(getString(path));
    }

    public void sendPresetMessage(String path, Player player, Object[] placeholders) {
        player.sendMessage(
            messages.getString(path).formatted(placeholders)
        );
    }

    public void sendPresetMessagePrefix(String path, Player player) {
        player.sendMessage(getString(path, prefix));
    }

    public void sendPresetMessagePrefix(String path, Player player, Object[] placeholders) {
        player.sendMessage(
            getString(path, prefix).formatted(placeholders)
        );
    }


    public void sendMessage(String message, Player player) {
        player.sendMessage(TextFormat.colorize(message));
    }

    public void sendMessage(String message, Player player, Object[] placeholders) {
        player.sendMessage(
            TextFormat.colorize(message.formatted(placeholders))
        );
    }

    public void sendMessagePrefix(String message, Player player) {
        player.sendMessage(TextFormat.colorize(prefix + message));
    }

    public void sendMessagePrefix(String message, Player player, Object[] placeholders) {
        player.sendMessage(
            TextFormat.colorize(prefix + message.formatted(placeholders))
        );
    }


    public String getPrefix() {
        return prefix;
    }
    
}
