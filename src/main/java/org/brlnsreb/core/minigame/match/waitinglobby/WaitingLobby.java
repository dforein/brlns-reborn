package org.brlnsreb.core.minigame.match.waitinglobby;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.waitinglobby.items.WaitingLobbyItemManager;
import org.brlnsreb.core.minigame.match.waitinglobby.ui.WaitingLobbyBossBar;
import org.brlnsreb.core.minigame.match.waitinglobby.ui.WaitingLobbyScoreboard;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.VotingSystem;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;

public abstract class WaitingLobby extends Lobby {

    protected final Set<CustomPlayer> players;

    protected final int minPlayers;
    protected final int maxPlayers;
    protected final int minPlayersShortenedCountdown;

    protected final int secondsCountdown;
    protected final int secondsShortenedCountdown;

    protected boolean countdownShortened = false;

    protected final NPCEntity leaveNpc;

    protected final Messages msgUtil;
    protected final WaitingLobbyBossBar bossBar;
    protected final WaitingLobbyItemManager items;
    protected final WaitingLobbyScoreboard scoreboard;

    protected TimerSystem timer;
    protected final VotingSystem<String> mapVoting;

    protected String selectedMap;

    public WaitingLobby(MinigameMatch match) {
        super(match);

        this.players = match.getPlayers();

        this.minPlayers = minigame.getMinPlayers();
        this.maxPlayers = minigame.getMaxPlayers();
        this.minPlayersShortenedCountdown = config.getInt("settings.min-players-shortened-countdown");

        Config globalConfig = ConfigManager.getConfig("global/config.yml");
        this.secondsCountdown = globalConfig.getInt(configPath() + "countdown-seconds");
        this.secondsShortenedCountdown = globalConfig.getInt(configPath() + "shortened-countdown-seconds");

        this.leaveNpc = spawnNpc(
            configPath() + "npc.", 
            (CustomPlayer player) -> { match.onLeave(player); }
        );

        this.msgUtil = match.getMsgUtil();
        this.bossBar = new WaitingLobbyBossBar(this, secondsCountdown);
        this.scoreboard = new WaitingLobbyScoreboard(match);
        this.items = requireItemManager();

        this.mapVoting = new VotingSystem<>();
    }


    //join-leave logic

    @Override
    public boolean onJoin(CustomPlayer player) {
        if (players.size() >= maxPlayers) {
            minigame.onMatchCreation();
            return false;
        }

        if (players.contains(player)) return false;
        
        players.add(player);
        super.onJoin(player);

        bossBar.updateWaitingLobbyBossBar(player);
        scoreboard.updateWaitingLobby(player);

        Messages.sendActionBar(
            players, 
            "action-bar.on-join", 
            new Object[] {player.getName(), players.size(), maxPlayers},
            ConfigManager.getConfig("global/messages")
        );

        checkPlayerNumber();

        return true;
    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.WAITING_LOBBY; 
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateWaitingLobbyBossBar();
    }

    protected void onJoinItems(CustomPlayer player) {
        if (countdownShortened) {
            items.giveItemsCountdownShortened(player);
        } else if (match.getCurrentState() == GameStateType.LOBBY_COUNTDOWN) {
            items.giveItemsCountdown(player);
        } else {
            items.giveItemsWaitingPlayers(player);
        }
    }

    public void onLeave(CustomPlayer player) {
        checkPlayerNumber();
    }


    //countdown logic

    protected void checkPlayerNumber() {
        if (players.size() > minPlayersShortenedCountdown) {
            shortenCountdown(true);
            
        } else if (players.size() > minPlayers && !countdownShortened) {    //if the countdown is already shortened, it will stay shortened
            match.getState().current = GameStateType.LOBBY_COUNTDOWN;
            startCountdown();

        } else if (match.getCurrentState() == GameStateType.LOBBY_COUNTDOWN) {  //not enough players
            match.getState().current = GameStateType.WAITING_LOBBY;
            stopCountdown();
        }
    }

    protected void startCountdown() {
        countdownShortened = false;
        
        PlayerUtils.clearInventory(players);
        
        if (timer != null) timer.stop();
        timer = new TimerSystem(secondsCountdown);
        timer.start(secondsCountdown, null, () -> {
            int remaining = timer.getSecondsRemaining();

            if (remaining == secondsShortenedCountdown) {
                shortenCountdown(false);
            }

            bossBar.updateWaitingLobbyBossBar(remaining);
        });
    }

    protected void shortenCountdown(boolean sendMessage) {
        countdownShortened = true;
        finalizeVoting();

        PlayerUtils.clearInventory(players);

        if (timer != null) timer.stop();
        timer = new TimerSystem(secondsShortenedCountdown);
        timer.start(secondsCountdown, match::onGameStart, () -> {
            int remaining = timer.getSecondsRemaining();

            bossBar.updateWaitingLobbyBossBar(remaining);
            
            float pitch = ThreadLocalRandom.current().nextFloat(0.9f, 1.01f);
            for (Player p : players) {
                p.getLevel().addSound(p, Sound.RANDOM_CLICK, 1.0f, pitch, p);
            }
        });

        match.preloadGame(selectedMap);

        if (sendMessage) {
            msgUtil.broadcastPresetPrefix("waiting-lobby.countdown-shortened");
        }
    }

    protected void stopCountdown() {
        if (timer != null) timer.stop();
        timer = null;
        bossBar.cancelCountdown();
        match.unloadGame();
    }


    //voting logic

    protected void prepareVoting() {
        if (!mapVoting.getAvailableOptions().isEmpty()) return;     //already prepared, skip code
        
        List<String> availableMaps = minigame.getAvailableMaps();
        
        while (availableMaps.size() > 3) {
            availableMaps.remove(
                ThreadLocalRandom.current().nextInt(availableMaps.size())
            );
        }

        mapVoting.setAvailableOptions(availableMaps);
    }

    protected void finalizeVoting() {
        if (selectedMap != null) return;

        selectedMap = mapVoting.getMostVoted();

        if (selectedMap == null) {
            List<String> availableMaps = minigame.getAvailableMaps();
            if (!availableMaps.isEmpty()) {
                selectedMap = availableMaps.get(new Random().nextInt(availableMaps.size()));
                BrlnsReb.getInstance().getLogger().warning("No vote. Fallback on random map: " + selectedMap);
            } else {
                BrlnsReb.getInstance().getLogger().error("CRITIC ERROR: No map enabled in config!");
                msgUtil.broadcastPrefix("§cError: no map available! Match cancelled.");
                match.stopMatch();
                return;
            }
        }
    }

    public abstract WaitingLobbyItemManager requireItemManager();
    public String requireConfigPath() { return "waiting-lobby."; }
    public Set<CustomPlayer> getPlayers() { return players; }
    
}