package org.brlnsreb.core.minigame.match.waitinglobby.ui;

import java.util.Set;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

import org.powernukkitx.Player;
import org.powernukkitx.utils.Config;

public class WaitingLobbyBossBar extends BossBarAbstract {

    private final String PATH = "match.waiting-lobby.bossbar.";
    private final Config config;

    private final Set<CustomPlayer> players;

    private final int mediumThreshold;
    private final int shortThreshold;

    private Integer currentSeconds = null;
    private final int secondsCountdown;

    public WaitingLobbyBossBar(WaitingLobby waitingLobby, int secondsCountdown) {
        this.config = ConfigManager.getGlobalConfig();

        this.players = waitingLobby.getPlayers();

        this.mediumThreshold = config.getInt(PATH + "medium-threshold");
        this.shortThreshold = config.getInt(PATH + "short-threshold");

        this.secondsCountdown = secondsCountdown;
    }

    public void updateWaitingLobbyBossBar() {
        for (Player p : players) {
            updateBossBar(
                (CustomPlayer) p, 
                YamlUtil.getStr(PATH + "text-waiting-players", config)
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
                YamlUtil.getStr(PATH + "text-waiting-players", config)
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
            return YamlUtil.getStr(PATH + "text-short", config)
                .formatted(seconds);
        } else if (seconds < mediumThreshold) {
            return YamlUtil.getStr(PATH + "text-medium", config)
                .formatted(seconds);
        } else {
            return YamlUtil.getStr(PATH + "text-long", config)
                .formatted(seconds / 60, seconds % 60);
        }
    }

}
