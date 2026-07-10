package org.brlnsreb.utils;

import java.util.Collection;
import java.util.Set;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class Messages {

    private Config messages;
    private final Collection<? extends Player> players;
    private final String prefix;
    
    public Messages(Config messages, Collection<? extends Player> players) {
        this.messages = messages;
        this.players = players;
        this.prefix = TextFormat.colorize(messages.getString("prefix") + " &r");
    }

    public static String getStrPrefix(String path, Config messages, String colorizedPrefix) {
        return colorizedPrefix + YamlUtil.getStr(path, messages);
    }

    public String getStrPrefix(String path, Config messages) {
        return prefix + YamlUtil.getStr(path, messages);
    }

    private String getStringPrefix(String path) {
        return getStrPrefix(path, this.messages);
    }

    private String getString(String path) {
        return YamlUtil.getStr(path, this.messages);
    }


    //action bar

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


    //title

    public void sendTitle(String pathTitle, String pathSubTitle) {
        for (Player p : players) {
            p.sendTitle(getString(pathTitle), getString(pathSubTitle));
        }
    }


    //broadcast to players a preset message

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
            getStringPrefix(path), 
            players
        );
    }

    public void broadcastPresetPrefix(String path, Object[] placeholders) {
        Server.getInstance().broadcastMessage(
            getStringPrefix(path).formatted(placeholders), 
            players
        );
    }


    //broadcast to players a custom message

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
            prefix + TextFormat.colorize(message), 
            players
        );
    }

    public void broadcastPrefix(String message, Object[] placeholders) {
        Server.getInstance().broadcastMessage(
            prefix + TextFormat.colorize(message.formatted(placeholders)), 
            players
        );
    }


    //send individually a preset message

    public void sendPresetMessage(String path, Player player) {
        player.sendMessage(getString(path));
    }

    public void sendPresetMessage(String path, Player player, Object[] placeholders) {
        player.sendMessage(
            messages.getString(path).formatted(placeholders)
        );
    }

    public void sendPresetMessagePrefix(String path, Player player) {
        player.sendMessage(getStringPrefix(path));
    }

    public void sendPresetMessagePrefix(String path, Player player, Object[] placeholders) {
        player.sendMessage(
            getStringPrefix(path).formatted(placeholders)
        );
    }


    //send individually a custom message

    public void sendMessage(String message, Player player) {
        player.sendMessage(TextFormat.colorize(message));
    }

    public void sendMessage(String message, Player player, Object[] placeholders) {
        player.sendMessage(
            TextFormat.colorize(message.formatted(placeholders))
        );
    }

    public void sendMessagePrefix(String message, Player player) {
        player.sendMessage(prefix + TextFormat.colorize(message));
    }

    public void sendMessagePrefix(String message, Player player, Object[] placeholders) {
        player.sendMessage(
            prefix + TextFormat.colorize(message.formatted(placeholders))
        );
    }


    public String getPrefix() { return prefix; }
    
}
