package com.brlnsreb.minigames.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;

public abstract class BossBarAbstract {
    
    protected final Map<UUID, Long> bossBarIds;
    
    public BossBarAbstract() {
        this.bossBarIds = new HashMap<>();
    }

    public void updateCountdown(Player player, String message, int seconds, int maxSeconds) {
        //specialized for countdowns
        float progress = (maxSeconds > 0) ? (float) seconds / maxSeconds : 0;
        updateBossBar(player, message, (int)(progress * 100));
    }

    protected void updateBossBar(Player player, String message) {
        //starts full, stays full
        updateBossBar(player, message, 100);
    }

    protected void updateBossBar(Player player, String message, int length) {
        //starts full, variates lenght
        Long bossBarId = bossBarIds.get(player.getUniqueId());
        if (bossBarId != null) {
            player.updateBossBar(message, length, bossBarId);
        } else {
            newBossBar(player, message, 100.0f);
        }
    }

    protected void updateBossBarPrecise(Player player, String message, int length) {
        //variates lenght already from start
        Long bossBarId = bossBarIds.get(player.getUniqueId());
        if (bossBarId != null) {
            player.updateBossBar(message, length, bossBarId);
        } else {
            newBossBar(player, message, (float) length);
        }
    }

    protected void newBossBar(Player player, String text, float length) {
        //creates a new bossbar
        clearAll(player);

        DummyBossBar bossBar = buildBossBar(player, text, length);

        long bossBarId = player.createBossBar(bossBar);
        bossBarIds.put(player.getUniqueId(), bossBarId);
    }

    protected DummyBossBar buildBossBar(Player player, String text, float length) {
        return new DummyBossBar.Builder(player)
            .text(text)
            .length(length)                 //full: 100.0f (float) / 100 (int)
            .color(BossBarColor.PURPLE)
            .build();
    }
    
    public void remove(Player player) {
        //removes the bossbar associated to the player, saved in the map
        Long bossBarId = bossBarIds.remove(player.getUniqueId());
        if (bossBarId != null) {
            player.removeBossBar(bossBarId);
        }
    }

    public void clearAll(Player player) {
        //removes all bossbars associated to the player
        if (!player.getDummyBossBars().isEmpty()) {
            for (DummyBossBar bar : player.getDummyBossBars().values()) {
                bar.destroy(); 
            }
        }
    }
    
    public void clear(List<Player> players) {
        //reset function
        bossBarIds.clear();

        for (Player p : players) {
            clearAll(p);
        }
    }
}