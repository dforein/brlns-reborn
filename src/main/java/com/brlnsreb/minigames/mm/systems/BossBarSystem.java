package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;

import java.util.HashMap;
import java.util.Map;

public class BossBarSystem {
    
    private final Map<String, Long> bossBarIds;
    
    public BossBarSystem() {
        this.bossBarIds = new HashMap<>();
    }
    
    public void showExpAndGold(Player player, int goldCount, int expCount) {
        DummyBossBar bossBar = new DummyBossBar.Builder(player)
            .text("§l§7- §a" + expCount + " §2EXP §7¦ §e" + goldCount + " GOLD §7-")
            .length(100.0f)
            .color(BossBarColor.PURPLE)
            .build();
        
        long bossBarId = player.createBossBar(bossBar);
        bossBarIds.put(player.getName(), bossBarId);
    }

    public void showExp(Player player, int expCount) {
        DummyBossBar bossBar = new DummyBossBar.Builder(player)
            .text("§l§7- §a" + expCount + " §2EXP §7-")
            .length(100.0f)
            .color(BossBarColor.PURPLE)
            .build();
        
        long bossBarId = player.createBossBar(bossBar);
        bossBarIds.put(player.getName(), bossBarId);
    }

    public void showExpAndDistance(Player player, int expCount, double distance) {
        DummyBossBar bossBar = new DummyBossBar.Builder(player)
            .text(String.format("§l§7- §aNEAREST: %.2fm §7¦ §a%d §2EXP §7-", distance, expCount))
            .length(100.0f)
            .color(BossBarColor.PURPLE)
            .build();
        
        long bossBarId = player.createBossBar(bossBar);
        bossBarIds.put(player.getName(), bossBarId);
    }
    
    public void updateExpAndGold(Player player, int goldCount, int expCount) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            player.updateBossBar(
                "§l§7- §a" + expCount + " §2EXP §7¦ §e" + goldCount + " GOLD §7-", 
                100, 
                bossBarId
            );
        }
    }

    public void updateExp(Player player, int expCount) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            player.updateBossBar(
                "§l§7- §a" + expCount + " §2EXP §7-", 
                100, 
                bossBarId
            );
        }
    }

    public void updateExpWithDistance(Player player, int expCount, double distance) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            player.updateBossBar(
                String.format("§l§7- §aNEAREST: %.2fm §7¦ §a%d §2EXP §7-", distance, expCount),
                100,
                bossBarId
            );
        }
    }

    public void showCountdown(Player player, int seconds, String message) {
        DummyBossBar bossBar = new DummyBossBar.Builder(player)
            .text(message)
            .length(100.0f)
            .color(BossBarColor.PURPLE)
            .build();
        
        long id = player.createBossBar(bossBar);
        bossBarIds.put(player.getName(), id);
    }

    public void updateCountdown(Player player, String message, int seconds, int maxSeconds) {
        Long id = bossBarIds.get(player.getName());
        if (id == null) return;
        
        float progress = (maxSeconds > 0) ? (float) seconds / maxSeconds : 0;
        player.updateBossBar(message, (int)(progress * 100), id);
    }
    
    public void hide(Player player) {
        Long bossBarId = bossBarIds.remove(player.getName());
        if (bossBarId != null) {
            player.removeBossBar(bossBarId);
        }
    }
    
    public void clear() {
        bossBarIds.clear();
    }
}