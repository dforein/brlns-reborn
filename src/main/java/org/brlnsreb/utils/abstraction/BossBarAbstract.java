package org.brlnsreb.utils.abstraction;

import java.util.Collection;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;

import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.TextFormat;

public abstract class BossBarAbstract {

    public void updateCountdown(CustomPlayer player, String message, int seconds, int maxSeconds) {
        //specialized for countdowns
        float progress = (maxSeconds > 0) ? (float) seconds / maxSeconds : 0;
        updateBossBar(player, message, (int)(progress * 100));
    }

    protected void updateBossBar(CustomPlayer player, String message) {
        //starts full, stays full
        updateBossBar(player, message, 100);
    }

    protected void updateBossBar(CustomPlayer player, String message, int length) {
        //starts full, variates lenght
        message = TextFormat.colorize(message);

        if (player.bossBarId != null) {
            player.updateBossBar(message, length, player.bossBarId);
        } else {
            newBossBar(player, message, 100.0f);
        }
    }

    protected void updateBossBarPrecise(CustomPlayer player, String message, int length) {
        //variates lenght already from start
        message = TextFormat.colorize(message);
        
        if (player.bossBarId != null) {
            player.updateBossBar(message, length, player.bossBarId);
        } else {
            newBossBar(player, message, (float) length);
        }
    }

    protected void newBossBar(CustomPlayer player, String text, float length) {
        //creates a new bossbar
        PlayerUtils.removeBossBar(player);

        DummyBossBar bossBar = buildBossBar(player, text, length);
        player.bossBarId = player.createBossBar(bossBar);
    }

    protected DummyBossBar buildBossBar(CustomPlayer player, String text, float length) {
        return new DummyBossBar.Builder(player)
            .text(text)
            .length(length)                 //full: 100.0f (float) / 100 (int)
            .color(BossBarColor.PURPLE)
            .build();
    }
    
    public void clear(Collection<CustomPlayer> players) {
        //reset function
        for (CustomPlayer p : players) {
            PlayerUtils.removeBossBar(p);
        }
    }
}