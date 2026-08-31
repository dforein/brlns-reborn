package org.brlnsreb.core.player;

import java.util.Collection;
import java.util.UUID;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.utils.abstraction.ScoreboardAbstract;
import org.cloudburstmc.protocol.bedrock.data.BuildPlatform;
import org.cloudburstmc.protocol.bedrock.data.payload.list.PlayerListAddEntry;
import org.cloudburstmc.protocol.bedrock.data.payload.list.PlayerListRemoveEntry;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;

import org.powernukkitx.Player;
import org.powernukkitx.PlayerFood;
import org.powernukkitx.Server;
import org.powernukkitx.entity.Attribute;
import org.powernukkitx.entity.effect.Effect;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Location;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.scoreboard.data.DisplaySlot;
import org.powernukkitx.utils.DummyBossBar;
import org.powernukkitx.utils.SkinConverter;

public class PlayerUtils {

    public static int onlinePlayers = 0;

    //change world

    public static PlayerStateType changeWorld(CustomPlayer p, Location loc, boolean lobby) {
        try {
            PlayerStateType oldState = p.state;
            Level oldLevel = p.getLevel();

            p.setTeleporting();
            p.despawnFromAll();

            if (lobby) {
                lobbyTeleport(p, loc);
            } else {
                p.setMotion(Vector3.ZERO);
                p.teleport(loc.add(0, 0.07, 0));    //adding y component to avoid falling through blocks (e.g. when tp on carpets)
            }

            p.getInventory().setHeldItemIndex(0);

            BrlnsReb.getScheduler().scheduleDelayedTask(BrlnsReb.instance, () -> p.spawnToAll(), 20);

            updatePlayerList(oldLevel, p);
            updateOnlinePlayer(p, true);    //remove the name for players who aren't in the same level

            return oldState;

        } catch (Exception e) {
            BrlnsReb.logger.error("Error teleporting player: " + e.getMessage());
            return null;
        }
    }


    //lobby-specific

    public static void lobbyTeleport(CustomPlayer p, Location loc) {
        p.teleport(loc.add(0.0, 1.0, 0.0));
        p.setMotion(new Vector3(0.0, 0.5, 0.0));
    }

    public static void setLobbyState(CustomPlayer p, PlayerStateType oldState, PlayerStateType newState) {
        if (!p.isOnline()) return;

        resetUiAndInventories(p);
        p.state = newState;

        if (oldState != null) {
            switch (oldState) {
                case LOBBY, WAITING_LOBBY, DEATH_LOBBY:
                    p.setPresetNameTag();
                    return;                     //already coming from a lobby, no need to execute the following code
                
                case SPECTATOR:
                    p.setFlying(false);
                    p.waitForAck(() -> p.setAllowFlight(false));
                    break;

                default: break;
            }
        }

        p.updatePresetNameTags();
        p.updateExp();
        
        resetVars(p);
        resetPlayer(p, Player.ADVENTURE, 18);

        giveEffect(p, EffectType.NIGHT_VISION, 99999999, 2, false);
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

        PlayerFood fd = p.getFoodData();
        fd.setFood(food);
        fd.setSaturation((float) food);
        fd.setExhaustion(0.0F);
        fd.setEnabled(false);
    }

    public static void sendFood(CustomPlayer p, int food) {
        Attribute attribute = p.getAttributes().computeIfAbsent(Attribute.FOOD, Attribute::getAttribute);
        attribute.setValue(food);
        p.syncAttribute(attribute);
    }


    //ui

    public static void removeScoreboard(CustomPlayer p) {
        if (!p.isOnline()) return;
        if (p.hasScoreboard()) {
            p.removeScoreboard();
            p.getScoreboard().removeViewer(p, DisplaySlot.SIDEBAR);
            p.resetScoreboard();
        }
        ScoreboardAbstract.remove(p);
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

    public static void initPlayerList(Player viewer, Level level) {
        for (Player p : level.getPlayers().values()) {
            addViewerToOnlinePlayer(p, viewer);
        }
    }

    public static void updatePlayerList(Player viewer, Level newLevel) {
        updatePlayerList(viewer, viewer.getLevel(), newLevel);
    }

    public static void updatePlayerList(Level oldLevel, Player viewer) {
        updatePlayerList(viewer, oldLevel, viewer.getLevel());
    }

    private static void updatePlayerList(Player viewer, Level oldLevel, Level newLevel) {
        for (Player p : oldLevel.getPlayers().values()) {
            if (p == viewer) continue;
            removeViewerToOnlinePlayer(p, viewer);
        }

        for (Player p : newLevel.getPlayers().values()) {
            if (p == viewer) continue;
            addViewerToOnlinePlayer(p, viewer);
        }
    }

    public static void cleanPlayerList(Player viewer) {
        Collection<Player> sameLevelPlayers = viewer.getLevel().getPlayers().values();

        for (Player p : Server.getInstance().getOnlinePlayers().values()) {
            if (p == viewer || sameLevelPlayers.contains(p)) continue;
            removeViewerToOnlinePlayer(p, viewer);
        }
    }

    public static void updateOnlinePlayer(Player player, boolean removeAllServer) {
        removeViewersToOnlinePlayer(player, removeAllServer);
        addViewersToOnlinePlayer(player, false);
    }

    public static void addViewerToOnlinePlayer(Player viewer, Player player) {
        viewer.sendPacket(addOnlinePlayerPacket(player));
    }

    public static void addViewersToOnlinePlayer(Player player, boolean allServer) {
        sendPlayerListPacket(
            addOnlinePlayerPacket(player), player, 
            allServer ? null : player.getLevel()
        );
    }

    public static void removeViewerToOnlinePlayer(Player player, Player target) {
        target.sendPacket(removeOnlinePlayerPacket(player));
    }

    public static void removeViewersToOnlinePlayer(Player player, boolean allServer) {
        sendPlayerListPacket(
            removeOnlinePlayerPacket(player), player, 
            allServer ? null : player.getLevel()
        );
    }

    private static void sendPlayerListPacket(PlayerListPacket pk, Player player, Level level) {
        Collection<Player> players;
        if (level != null) players = level.getPlayers().values();
        else players = Server.getInstance().getOnlinePlayers().values();
        
        for (Player p : players) {
            if (p == player) continue;
            p.sendPacket(pk);
        }
    }

    @SuppressWarnings("deprecation")
    public static PlayerListPacket addOnlinePlayerPacket(Player player) {
        final PlayerListPacket pk = new PlayerListPacket();

        final PlayerListAddEntry entry = new PlayerListAddEntry();
        entry.setUuid(player.getUniqueId());
        entry.setActorUniqueID(player.getId());
        entry.setPlayerName(player.getName());
        entry.setXblXUID(player.getXUID());
        entry.setPlatformOnlineID("");
        entry.setBuildPlatform(BuildPlatform.UNKNOWN);
        entry.setSkin(player.getSkin().getSkin());
        entry.setSerializedSkin(SkinConverter.toSerializedSkin(player.getSkin().getSkin(), player.getSkin().isTrusted()));
        entry.setPlayerColor(player.getLocatorBarColor().getRGB());

        pk.getEntries().add(entry);
        
        return pk;
    }

    public static PlayerListPacket removeOnlinePlayerPacket(Player player) {
        final PlayerListPacket pk = new PlayerListPacket();

        final PlayerListRemoveEntry entry = new PlayerListRemoveEntry();
        entry.setUuid(player.getUniqueId());

        pk.getEntries().add(entry);

        return pk;
    }


    //misc

    public static void giveNightVision(Player player) {
        PlayerUtils.giveEffect(
            player, 
            EffectType.NIGHT_VISION, 
            99999999, 
            2, 
            false
        );
    }

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