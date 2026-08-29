package org.brlnsreb.core.minigame.match.waitinglobby.ui;

import java.util.Set;

import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.abstraction.BossBarAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.powernukkitx.Player;
import org.powernukkitx.utils.Config;

public class WaitingLobbyBossBar extends BossBarAbstract {

    private final String PATH = "match.waiting-lobby.bossbar.";
    private final Config config;

    private final Set<CustomPlayer> players;

    private final int mediumThreshold;
    private final int shortThreshold;

    private int currentSeconds = -1;
    private final int secondsCountdown;

    public WaitingLobbyBossBar(WaitingLobby waitingLobby, int secondsCountdown) {
        this.config = Configs.getGlobalConfig();

        this.players = waitingLobby.getPlayers();

        this.mediumThreshold = config.getInt(PATH + "medium-threshold-seconds");
        this.shortThreshold = config.getInt(PATH + "short-threshold-seconds");

        this.secondsCountdown = secondsCountdown;
    }

    public void updateWaitingLobby() {
        for (Player p : players) {
            updateBossBar(
                (CustomPlayer) p, 
                YamlUtil.getStr(PATH + "text-waiting-players", config)
            );
        }
    }

    public void updateLobbyCountdown(int seconds) {
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

    public void updatePlayer(Player player) {
        if (this.currentSeconds < 0) {
            updateBossBar(
                (CustomPlayer) player, 
                YamlUtil.getStr(PATH + "text-waiting-players", config)
            );
        } else {
            updateCountdown(
                (CustomPlayer) player, 
                formatCountdownMessage(this.currentSeconds),
                this.currentSeconds,
                secondsCountdown
            );
        }
    }

    public void cancelCountdown() {
        this.currentSeconds = -1;
        updateWaitingLobby();
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
