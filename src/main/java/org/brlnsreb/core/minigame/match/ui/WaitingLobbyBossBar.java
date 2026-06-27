package org.brlnsreb.core.minigame.match.ui;

import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

import cn.nukkit.utils.Config;

public class WaitingLobbyBossBar extends BossBarAbstract {

    private final Config config;
    private final String configPath = "waiting-lobby-bossbar.";

    private final int mediumThreshold;
    private final int shortThreshold;

    private boolean countdownShortened;

    public WaitingLobbyBossBar(Config config) {
        this.config = config;

        this.mediumThreshold = config.getInt(configPath + "medium-threshold");
        this.shortThreshold = config.getInt(configPath + "short-threshold");
    }

    public void updateWaitingLobbyBossBar(int seconds, boolean countdownShortened) {
        if (this.countdownShortened == countdownShortened) {
            
        }
    }

    private String formatCountdownMessage(int seconds) {
        if (seconds <= shortThreshold) {
            return YamlUtil.getStr(configPath + "text-short", config)
                .formatted(seconds);
        } else if (seconds < mediumThreshold) {
            return YamlUtil.getStr(configPath + "text-medium", config)
                .formatted(seconds);
        } else {
            return YamlUtil.getStr(configPath + "text-long", config)
                .formatted(seconds / 60, seconds % 60);
        }
    }

}
