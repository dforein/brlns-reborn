package org.brlnsreb.mm.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

public class BossBarSystem extends BossBarAbstract {
    
    public void updateExpAndGold(CustomPlayer player, int goldCount, int expCount) {
        updateBossBar(player,
            "§l§7- §a" + expCount + " EXP §7¦ §e" + goldCount + " GOLD §7-"
        );

    }

    public void updateExp(CustomPlayer player, int expCount) {
        updateBossBar(player,
            "§l§7- §a" + expCount + " EXP §7-"
        );
    }

    public void updateExpAndDistance(CustomPlayer player, int expCount, double distance) {
        updateBossBar(player,
            "§l§7- §aNEAREST: %.2fm §7¦ §a%d EXP §7-".formatted(distance, expCount)
        );
    }
}