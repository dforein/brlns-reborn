package org.brlnsreb.core.minigame.match.waitinglobby.ui;

import java.util.Set;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public class WaitingLobbyBossBar extends BossBarAbstract {

    private final Set<CustomPlayer> players;

    private final Config config;
    private final String configPath = "match.waiting-lobby.bossbar.";

    private final int mediumThreshold;
    private final int shortThreshold;

    private Integer currentSeconds = null;
    private final int secondsCountdown;

    public WaitingLobbyBossBar(WaitingLobby waitingLobby, int secondsCountdown) {
        this.players = waitingLobby.getPlayers();

        this.config = ConfigManager.getGlobalConfig();

        this.mediumThreshold = config.getInt(configPath + "medium-threshold");
        this.shortThreshold = config.getInt(configPath + "short-threshold");

        this.secondsCountdown = secondsCountdown;
    }

    public void updateWaitingLobbyBossBar() {
        for (Player p : players) {
            updateBossBar(
                (CustomPlayer) p, 
                YamlUtil.getStr(configPath + "text-waiting-players", config)
            );
        }
    }

    public void updateWaitingLobbyBossBar(int seconds) {
        this.currentSeconds = seconds;

        for (Player p : players) {
            updateCountdown(
                (CustomPlayer) p,
                formatCountdownMessage(seconds),
                seconds,
                secondsCountdown
            );
        }
    }

    public void updateWaitingLobbyBossBar(Player player) {
        if (this.currentSeconds == null) {
            updateBossBar(
                (CustomPlayer) player, 
                YamlUtil.getStr(configPath + "text-waiting-players", config)
            );
        } else {
            updateCountdown(
                (CustomPlayer) player, 
                formatCountdownMessage(this.currentSeconds),
                currentSeconds,
                secondsCountdown
            );
        }
    }

    public void cancelCountdown() {
        this.currentSeconds = null;
        updateWaitingLobbyBossBar();
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
