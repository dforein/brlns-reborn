package com.brlnsreb.minigames.mm.ui;

import com.brlnsreb.minigames.utils.abstraction.BossBarAbstract;

import cn.nukkit.Player;

public class BossBarSystem extends BossBarAbstract {
    
    public BossBarSystem() {
        super();
    }
    
    public void updateExpAndGold(Player player, int goldCount, int expCount) {
        updateBossBar(player,
            "§l§7- §a" + expCount + " EXP §7¦ §e" + goldCount + " GOLD §7-"
        );

    }

    public void updateExp(Player player, int expCount) {
        updateBossBar(player,
            "§l§7- §a" + expCount + " EXP §7-"
        );
    }

    public void updateExpAndDistance(Player player, int expCount, double distance) {
        updateBossBar(player,
            "§l§7- §aNEAREST: %.2fm §7¦ §a%d EXP §7-".formatted(distance, expCount)
        );
    }
}