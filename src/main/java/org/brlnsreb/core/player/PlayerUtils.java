package org.brlnsreb.core.player;

import java.util.Collection;
import java.util.UUID;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.cloudburstmc.protocol.bedrock.data.PlayerListPacketType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.entity.effect.Effect;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.scoreboard.data.DisplaySlot;
import org.powernukkitx.utils.DummyBossBar;

public class PlayerUtils {

    //change world

    public static void changeWorld(CustomPlayer p, Position pos, boolean lobby) {
        BrlnsReb plugin = BrlnsReb.instance;

        try {
            int viewDistance = p.getViewDistance();

            p.setTeleporting();
            p.setViewDistance(2);
            p.despawnFromAll();

            if (lobby) lobbyTeleport(p, pos);
            else p.teleport(pos);

            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                p.spawnToAll(); 
                p.setViewDistance(viewDistance);
            }, 20);

            updateOnlinePlayer(p, true);    //remove the name for players who aren't in the same level

        } catch (Exception e) {
            plugin.getLogger().error("Error teleporting player: " + e.getMessage());
        }
    }


    //lobby-specific

    public static void lobbyTeleport(CustomPlayer p, Position pos) {
        p.teleport(pos.add(0.0, 1.0, 0.0));
        p.setMotion(new Vector3(0.0, 0.42, 0.0));
    }

    public static void setLobbyState(CustomPlayer p, PlayerStateType newState) {
        if (!p.isOnline()) return;

        resetUiAndInventories(p);

        PlayerStateType oldState = p.state; 
        p.state = newState;
        p.setPresetNameTag();

        switch (oldState) {
            case LOBBY, WAITING_LOBBY, END_LOBBY:
                return;                 //already coming from a lobby, no need to execute the following code
        
            default:
                break;
        }
        
        resetVars(p);
        resetPlayer(p, Player.ADVENTURE, 18);

        giveEffect(p, EffectType.NIGHT_VISION, 99999999, 2, false);
        p.updateExp();
    }


    //reset player

    public static void resetUiAndInventories(CustomPlayer p) {
        clearInventory(p);
        removeScoreboard(p);
        removeBossBar(p);
    }

    public static void resetVars(CustomPlayer p) {
        p.setAttackVars(DamageMode.INVULNERABLE, false, false);
        p.interactMode = InteractMode.LIMITED;
    }

    public static void resetPlayer(CustomPlayer p, int gamemode, int food) {
        p.setGamemode(gamemode);

        p.removeAllEffects();

        p.setHealthCurrent(p.getHealthMax());
        p.getFoodData().setEnabled(false);
        p.getFoodData().setFood(food);
    }


    //ui

    public static void removeScoreboard(CustomPlayer p) {
        if (!p.isOnline()) return;
        if (p.hasScoreboard()) {
            p.removeScoreboard();
            p.getScoreboard().removeViewer(p, DisplaySlot.SIDEBAR);
            p.resetScoreboard();
        }
    }

    public static void removeBossBar(CustomPlayer p) {
        if (!p.isOnline()) return;
        if (!p.getDummyBossBars().isEmpty()) {
            for (DummyBossBar bar : p.getDummyBossBars().values()) {
                bar.destroy(); 
            }
        }

        p.resetBossBarId();
    }


    //inventory

    public static void clearInventory(Collection<? extends Player> players) {
        for (Player p : players) {
            clearInventory(p);
        }
    }

    public static void clearInventory(Player p) {
        if (!p.isOnline()) return;
        
        p.getInventory().clearAll();
        p.getCursorInventory().clearAll();

        p.getInventory().sendContents(p);
        p.getCursorInventory().sendContents(p);
    }

    public static void clearItem(Player player, String itemId) {
        clearItem(player, itemId, true);
    }

    public static void clearItem(Player player, String itemId, boolean onlyFirstFound) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId().equals(itemId)) {
                player.getInventory().clear(i);
                if (onlyFirstFound) break;
            }
        }
    }
    
    public static void clearItem(Player player, String itemId, String tag) {
        clearItem(player, itemId, tag, true);
    }

    public static void clearItem(Player player, String itemId, String tag, boolean onlyFirstFound) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId().equals(itemId) && itemPointed.hasTag(tag)) {
                player.getInventory().clear(i);
                if (onlyFirstFound) break;
            }
        }
    }


    //player list packets

    public static void updateOnlinePlayer(Player player, boolean removeAllServer) {
        removeOnlinePlayer(player, removeAllServer);
        addOnlinePlayer(player, false);
    }

    public static void addOnlinePlayer(Player player, boolean allServer) {
        sendPlayerListPacket(player, PlayerListPacketType.ADD, allServer);
    }

    public static void removeOnlinePlayer(Player player, boolean allServer) {
        sendPlayerListPacket(player, PlayerListPacketType.REMOVE, allServer);
    }

    private static void sendPlayerListPacket(Player player, PlayerListPacketType action, boolean allServer) {
        final PlayerListPacket pk = new PlayerListPacket();
        pk.setAction(action);
        pk.getEntries().add(new PlayerListPacket.Entry(player.getUniqueId()));

        Collection<Player> players;
        if (allServer) players = Server.getInstance().getOnlinePlayers().values();
        else players = player.getLevel().getPlayers().values();
        
        for (Player p : players) {
            p.sendPacket(pk);
        }
    }


    //misc

    public static void giveEffect(Player player, EffectType type, int duration, int amplifier, boolean isVisible) {
        Effect effect = Effect.get(type);
        effect.setDuration(duration);
        effect.setAmplifier(amplifier);
        effect.setVisible(isVisible);
        player.addEffect(effect);
    }

    public static CustomPlayer getPlayer(String name) {
        return getPlayer(PlayerDataManager.getPlayerId(name));
    }

    public static CustomPlayer getPlayer(UUID uuid) {
        if (uuid == null) return null;
        return (CustomPlayer) Server.getInstance().getPlayer(uuid).orElse(null);
    }

}