package org.brlnsreb.core.player;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer.DamageState;

import cn.nukkit.Player;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.level.Position;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.utils.DummyBossBar;

public class PlayerUtils {
    
    public static void removeScoreboard(CustomPlayer p) {
        if (p.scoreboard != null) {
            p.removeScoreboard(p.scoreboard);
            p.scoreboard.removeViewer(p, DisplaySlot.SIDEBAR);
            p.scoreboard = null;
        }
    }

    public static void removeBossBar(CustomPlayer p) {
        if (!p.getDummyBossBars().isEmpty()) {
            for (DummyBossBar bar : p.getDummyBossBars().values()) {
                bar.destroy(); 
            }
        }

        p.bossBarId = null;
    }

    public static void changeWorld(Player p, Position pos) {
        BrlnsReb plugin = BrlnsReb.getInstance();

        try {
            int viewDistance = p.getViewDistance();

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
        clearInventory(p);
        removeScoreboard(p);
        removeBossBar(p);

        if (p.state == PlayerStateType.LOBBY || p.state == PlayerStateType.WAITING_LOBBY) {
            p.state = newState;
            p.resetNameTag();
            
            return;                 //already coming from a lobby, no need to execute the following code
        }

        p.state = newState;

        p.setAttackVars(DamageState.INVULNERABLE, false, false);
        p.setGamemode(Player.ADVENTURE);

        p.removeAllEffects();
        giveEffect(p, EffectType.NIGHT_VISION, 99999999, 2, false);

        p.setHealthCurrent(p.getHealthMax());
        p.getFoodData().setFood(18);
    }

    private static void giveEffect(Player player, EffectType type, int duration, int amplifier, boolean isVisible) {
        Effect effect = Effect.get(type);
        effect.setDuration(duration);
        effect.setAmplifier(amplifier);
        effect.setVisible(isVisible);
        player.addEffect(effect);
    }

    public static void clearInventory(Player p) {
        if (!p.isOnline()) return;
        
        p.getInventory().clearAll();
        p.getCursorInventory().clearAll();

        p.getInventory().sendContents(p);
        p.getCursorInventory().sendContents(p);
    }

}
