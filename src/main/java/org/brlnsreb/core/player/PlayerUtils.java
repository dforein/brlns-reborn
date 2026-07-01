package org.brlnsreb.core.player;

import java.util.Collection;
import java.util.UUID;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.utils.DummyBossBar;

public class PlayerUtils {
    
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

    public static void changeWorld(CustomPlayer p, Position pos) {
        BrlnsReb plugin = BrlnsReb.getInstance();

        try {
            int viewDistance = p.getViewDistance();

            p.setTeleporting();
            p.setViewDistance(2);
            p.despawnFromAll();

            p.teleport(pos);

            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                p.spawnToAll(); 
                p.setViewDistance(viewDistance);
            }, 20);

        } catch (Exception e) {
            plugin.getLogger().error("Error teleporting player: " + e.getMessage());
        }
    }

    public static void setLobbyState(CustomPlayer p, PlayerStateType newState) {
        if (!p.isOnline()) return;

        clearInventory(p);
        removeScoreboard(p);
        removeBossBar(p);

        if (p.state == PlayerStateType.LOBBY || p.state == PlayerStateType.WAITING_LOBBY) {
            p.state = newState;
            p.resetNameTag();
            
            return;                 //already coming from a lobby, no need to execute the following code
        }

        p.state = newState;
        p.setAttackVars(DamageMode.INVULNERABLE, false, false);
        p.interactMode = InteractMode.LIMITED;

        p.setGamemode(Player.ADVENTURE);

        p.removeAllEffects();
        giveEffect(p, EffectType.NIGHT_VISION, 99999999, 2, false);

        p.setHealthCurrent(p.getHealthMax());
        p.getFoodData().setFood(18);

        p.updateExp();
    }

    private static void giveEffect(Player player, EffectType type, int duration, int amplifier, boolean isVisible) {
        Effect effect = Effect.get(type);
        effect.setDuration(duration);
        effect.setAmplifier(amplifier);
        effect.setVisible(isVisible);
        player.addEffect(effect);
    }

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
        clearItem(player, itemId, false);
    }

    public static void clearItem(Player player, String itemId, boolean continueSearch) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId().equals(itemId)) {
                player.getInventory().clear(i);
                if (!continueSearch) break;
            }
        }
    }
    
    public static void clearItem(Player player, String itemId, String tag) {
        clearItem(player, itemId, tag, false);
    }

    public static void clearItem(Player player, String itemId, String tag, boolean continueSearch) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId().equals(itemId) && itemPointed.hasTag(tag)) {
                player.getInventory().clear(i);
                if (!continueSearch) break;
            }
        }
    }

    public static CustomPlayer getPlayer(UUID uuid) {
        return (CustomPlayer) Server.getInstance().getPlayer(uuid).orElse(null);
    }

}
