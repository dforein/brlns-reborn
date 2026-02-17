package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: bossbar astraction into Utils

public class BossBarSystem {
    
    private final Map<String, Long> bossBarIds;
    
    public BossBarSystem() {
        this.bossBarIds = new HashMap<>();
    }
    
    public void showExpAndGold(Player player, int goldCount, int expCount) {
        newBossBar(player,
            "§l§7- §a" + expCount + " EXP §7¦ §e" + goldCount + " GOLD §7-"
        );

    }

    public void showExp(Player player, int expCount) {
        newBossBar(player,
            "§l§7- §a" + expCount + " EXP §7-"
        );
    }

    public void showExpAndDistance(Player player, int expCount, double distance) {
        newBossBar(player,
            String.format("§l§7- §aNEAREST: %.2fm §7¦ §a%d EXP §7-", distance, expCount)
        );
    }

    public void showCountdown(Player player, String message) {
        newBossBar(player, message);
    }

    private void newBossBar(Player player, String text) {
        clearAll(player);

        DummyBossBar bossBar = buildBossBar(player, text);

        long bossBarId = player.createBossBar(bossBar);
        bossBarIds.put(player.getName(), bossBarId);
    }

    private DummyBossBar buildBossBar(Player player, String text) {
        return new DummyBossBar.Builder(player)
            .text(text)
            .length(100.0f)
            .color(BossBarColor.PURPLE)
            .build();
    }
    
    public void updateExpAndGold(Player player, int goldCount, int expCount) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            player.updateBossBar(
                "§l§7- §a" + expCount + " EXP §7¦ §e" + goldCount + " GOLD §7-", 
                100, 
                bossBarId
            );
        } else {
            showExpAndGold(player, goldCount, expCount);
        }
    }

    public void updateExp(Player player, int expCount) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            player.updateBossBar(
                "§l§7- §a" + expCount + " EXP §7-", 
                100, 
                bossBarId
            );
        } else {
            showExp(player, expCount);
        }
    }

    public void updateExpWithDistance(Player player, int expCount, double distance) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            player.updateBossBar(
                String.format("§l§7- §aNEAREST: %.2fm §7¦ §a%d EXP §7-", distance, expCount),
                100,
                bossBarId
            );
        } else {
            showExpAndDistance(player, expCount, distance);
        }
    }

    public void updateCountdown(Player player, String message, int seconds, int maxSeconds) {
        Long bossBarId = bossBarIds.get(player.getName());
        if (bossBarId != null) {
            float progress = (maxSeconds > 0) ? (float) seconds / maxSeconds : 0;
            player.updateBossBar(message, (int)(progress * 100), bossBarId);
        } else {
            showCountdown(player, message);
        }
    }
    
    public void hide(Player player) {
        Long bossBarId = bossBarIds.remove(player.getName());
        if (bossBarId != null) {
            player.removeBossBar(bossBarId);
        }
    }

    public void clearAll(Player player) {
        if (!player.getDummyBossBars().isEmpty()) {
            for (DummyBossBar bar : player.getDummyBossBars().values()) {
                bar.destroy(); 
            }
        }
    }
    
    public void clear(List<Player> players) {
        bossBarIds.clear();

        for (Player p : players) {
            clearAll(p);
        }
    }
}