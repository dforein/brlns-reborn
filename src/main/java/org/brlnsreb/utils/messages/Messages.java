package org.brlnsreb.utils.messages;

import java.util.Collection;

import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs.Alignment;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class Messages {

    private Config messages;
    private final Collection<? extends Player> players;
    private final Collection<? extends Player> spectators;
    private final String prefix;

    private final Server server;
    
    public Messages(Config messages, String prefix, Collection<? extends Player> players) {
        this(messages, prefix, players, null);
    }

    public Messages(Config messages, String prefix, Collection<? extends Player> players, Collection<? extends Player> spectators) {
        this.messages = messages;
        this.prefix = prefix;
        this.players = players;
        this.spectators = spectators;

        server = Server.getInstance();
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


    //message block

    public void sendMessageBlock(Alignment alignment, boolean addSpace, String... lines) {
        sendMessageBlock(players, alignment, addSpace, lines);
        if (spectators != null) sendMessageBlock(spectators, alignment, addSpace, lines);
    }

    public static void sendMessageBlock(Collection<? extends Player> players, Alignment alignment, boolean addSpace, String... lines) {
        String content = ChatMsgs.buildBlockContent(alignment, lines);

        for (Player p : players) {
            sendMessageBlock(p, addSpace, content);
        }
    }

    public static void sendMessageBlock(Player player, Alignment alignment, boolean addSpace, String... lines) {
        sendMessageBlock(
            player, 
            addSpace, 
            ChatMsgs.buildBlockContent(alignment, lines)
        );
    }

    private static void sendMessageBlock(Player player, boolean addSpace, String content) {
        player.sendMessage(ChatMsgs.BAR);
        if (addSpace) player.sendMessage("§2-§r");

        player.sendMessage(content);

        if (addSpace) player.sendMessage("§2-§r");
        player.sendMessage(ChatMsgs.BAR);
    }


    //action bar

    public static void sendActionBar(Collection<? extends Player> players, String path, Config messages, int duration) {
        for (Player p : players) {
            p.sendActionBar(YamlUtil.getStr(path, messages), 1, duration, 1);
        }
    }

    public static void sendActionBar(Collection<? extends Player> players, String path, Object[] placeholders, Config messages, int duration) {
        for (Player p : players) {
            p.sendActionBar(YamlUtil.getStr(path, messages).formatted(placeholders), 1, duration, 1);
        }
    }

    //title

    public void sendTitle(String pathTitle, String pathSubTitle) {
        sendTitle(pathTitle, pathSubTitle, players);
        if (spectators != null) sendTitle(pathTitle, pathSubTitle, spectators);
    }

    public void sendTitle(String pathTitle, String pathSubTitle, Collection<? extends Player> playerCollection) {
        for (Player p : playerCollection) {
            p.sendTitle(getString(pathTitle), getString(pathSubTitle), 10, 60, 10);
        }
    }


    //broadcast to players a preset message

    public void broadcastPreset(String path) {
        broadcastPreset(players, path);
        if (spectators != null) broadcastPreset(spectators, path);
    }

    public void broadcastPreset(Collection<? extends Player> playerCollection, String path) {
        server.broadcastMessage(
            getString(path), 
            playerCollection
        );
    }

    public void broadcastPreset(String path, Object[] placeholders) {
        broadcastPreset(players, path, placeholders);
        if (spectators != null) broadcastPreset(spectators, path, placeholders);
    }

    public void broadcastPreset(Collection<? extends Player> playerCollection, String path, Object[] placeholders) {
        server.broadcastMessage(
            getString(path).formatted(placeholders), 
            playerCollection
        );
    }

    public void broadcastPresetPrefix(String path) {
        broadcastPresetPrefix(players, path);
        if (spectators != null) broadcastPresetPrefix(spectators, path);
    }

    public void broadcastPresetPrefix(Collection<? extends Player> playerCollection, String path) {
        server.broadcastMessage(
            getStringPrefix(path), 
            playerCollection
        );
    }

    public void broadcastPresetPrefix(String path, Object[] placeholders) {
        broadcastPresetPrefix(players, path, placeholders);
        if (spectators != null) broadcastPresetPrefix(spectators, path, placeholders);
    }

    public void broadcastPresetPrefix(Collection<? extends Player> playerCollection, String path, Object[] placeholders) {
        server.broadcastMessage(
            getStringPrefix(path).formatted(placeholders), 
            playerCollection
        );
    }


    //broadcast to players a custom message

    public void broadcast(String message) {
        broadcast(players, message);
        if (spectators != null) broadcast(spectators, message);
    }

    public void broadcast(Collection<? extends Player> playerCollection, String message) {
        server.broadcastMessage(
            TextFormat.colorize(message), 
            playerCollection
        );
    }

    public void broadcast(String message, Object[] placeholders) {
        broadcast(players, message, placeholders);
        if (spectators != null) broadcast(spectators, message, placeholders);
    }

    public void broadcast(Collection<? extends Player> playerCollection, String message, Object[] placeholders) {
        server.broadcastMessage(
            TextFormat.colorize(message.formatted(placeholders)), 
            playerCollection
        );
    }

    public void broadcastPrefix(String message) {
        broadcastPrefix(players, message);
        if (spectators != null) broadcastPrefix(spectators, message);
    }

    public void broadcastPrefix(Collection<? extends Player> playerCollection, String message) {
        server.broadcastMessage(
            prefix + TextFormat.colorize(message), 
            playerCollection
        );
    }

    public void broadcastPrefix(String message, Object[] placeholders) {
        broadcastPrefix(players, message, placeholders);
        if (spectators != null) broadcastPrefix(spectators, message, placeholders);
    }

    public void broadcastPrefix(Collection<? extends Player> playerCollection, String message, Object[] placeholders) {
        server.broadcastMessage(
            prefix + TextFormat.colorize(message.formatted(placeholders)), 
            playerCollection
        );
    }


    //send individually a preset message

    public void sendPresetMessage(Player player, String path) {
        player.sendMessage(getString(path));
    }

    public void sendPresetMessage(Player player, String path, Object[] placeholders) {
        player.sendMessage(
            messages.getString(path).formatted(placeholders)
        );
    }

    public void sendPresetMessagePrefix(Player player, String path) {
        player.sendMessage(getStringPrefix(path));
    }

    public void sendPresetMessagePrefix(Player player, String path, Object[] placeholders) {
        player.sendMessage(
            getStringPrefix(path).formatted(placeholders)
        );
    }


    //send individually a custom message

    public void sendMessage(Player player, String message) {
        player.sendMessage(TextFormat.colorize(message));
    }

    public void sendMessage(Player player, String message, Object[] placeholders) {
        player.sendMessage(
            TextFormat.colorize(message.formatted(placeholders))
        );
    }

    public void sendMessagePrefix(Player player, String message) {
        player.sendMessage(prefix + TextFormat.colorize(message));
    }

    public void sendMessagePrefix(Player player, String message, Object[] placeholders) {
        player.sendMessage(
            prefix + TextFormat.colorize(message.formatted(placeholders))
        );
    }


    public String getPrefix() { return prefix; }
    
}