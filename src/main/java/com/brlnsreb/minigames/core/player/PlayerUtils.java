package com.brlnsreb.minigames.core.player;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.player.CustomPlayer.DamageState;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.utils.DummyBossBar;

public class PlayerUtils {
    
    public static void removeScoreboard(CustomPlayer p) {
        if (p.scoreboard != null) {
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
        MinigameCore plugin = MinigameCore.getInstance();

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

    public static void setLobbyState(CustomPlayer p) {
        p.state = PlayerStateType.LOBBY;
        
        p.setAttackVars(DamageState.INVULNERABLE, false, false);
        p.setGamemode(Player.ADVENTURE);

        p.removeAllEffects();
        p.setHealthCurrent(p.getHealthMax());
        p.getFoodData().setFood(18);

        p.resetNameTag();

        clearInventory(p);
        removeScoreboard(p);
        removeBossBar(p);
    }

    public static void clearInventory(Player p) {
        if (!p.isOnline()) return;
        
        p.getInventory().clearAll();
        p.getCursorInventory().clearAll();

        p.getInventory().sendContents(p);
        p.getCursorInventory().sendContents(p);
    }

}
