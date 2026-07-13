package org.brlnsreb.core.minigame.match.waitinglobby;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.minigame.match.waitinglobby.items.WaitingLobbyItemManager;
import org.brlnsreb.core.minigame.match.waitinglobby.ui.WaitingLobbyBossBar;
import org.brlnsreb.core.minigame.match.waitinglobby.ui.WaitingLobbyScoreboard;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.VotingSystem;
import org.brlnsreb.utils.voting.Weather;

import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Sound;
import org.powernukkitx.utils.Config;

public abstract class WaitingLobby extends Lobby {

    protected final Set<CustomPlayer> players;

    protected final int minPlayers;
    protected final int maxPlayers;
    protected final int minPlayersShortenedCountdown;

    protected final int secondsCountdown;
    protected final int secondsShortenedCountdown;
    protected boolean countdownShortened = false;

    protected final Messages msgUtil;
    protected final WaitingLobbyBossBar bossBar;
    protected final WaitingLobbyItemManager items;
    protected final WaitingLobbyScoreboard scoreboard;

    protected TimerSystem timer;
    protected VotingSystem<String> mapVoting;
    protected VotingSystem<TimeOfDay> timeVoting = null;
    protected VotingSystem<Weather> weatherVoting = null;

    protected String selectedMap;
    protected TimeOfDay selectedTime = null;
    protected Weather selectedWeather = null;

    public WaitingLobby(Match match) {
        super(match);

        this.players = match.getPlayers();

        this.minPlayers = minigame.getMinPlayers();
        this.maxPlayers = minigame.getMaxPlayers();
        this.minPlayersShortenedCountdown = config.getInt("settings.min-players-shortened-countdown");

        Config globalConfig = ConfigManager.getGlobalConfig();
        this.secondsCountdown = globalConfig.getInt(configPath() + "countdown-seconds");
        this.secondsShortenedCountdown = globalConfig.getInt(configPath() + "shortened-countdown-seconds");

        spawnNpc(
            "match." + configPath() + "npc.", 
            globalConfig,
            (CustomPlayer player) -> { match.onLeave(player); }
        );

        this.msgUtil = match.getMsgUtil();
        this.bossBar = new WaitingLobbyBossBar(this, secondsCountdown);
        this.scoreboard = new WaitingLobbyScoreboard(match);
        this.items = requireItemManager();

        this.mapVoting = new VotingSystem<>();
        requireVotingMenu();
    }


    //join-leave logic

    @Override
    public boolean onJoin(CustomPlayer player) {
        if (players.size() >= maxPlayers) {
            minigame.onReplacePendingMatch(match);
            return false;
        }

        if (player.isTeleporting()) return false;
        if (players.contains(player)) return false;
        
        players.add(player);
        super.onJoin(player);

        bossBar.updateWaitingLobbyBossBar(player);
        scoreboard.updateWaitingLobby(player);

        checkPlayerNumber(players.size());

        return true;
    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.WAITING_LOBBY; 
    }

    protected void onJoinMessages(CustomPlayer player) {
        Messages.sendActionBar(
            players, 
            "action-bar.on-join", 
            new Object[] {player.getName(), players.size(), maxPlayers},
            ConfigManager.getConfig("global/messages")
        );
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
        checkPlayerNumber(players.size() - 1);
    }


    //countdown logic

    protected void checkPlayerNumber(int playerNumber) {
        if (playerNumber >= maxPlayers) {
            minigame.onReplacePendingMatch(match);
            
        } else if (playerNumber > minPlayersShortenedCountdown) {
            shortenCountdown(true);
            
        } else if (playerNumber > minPlayers && !countdownShortened) {    //if the countdown is already shortened, it will stay shortened
            match.getState().current = GameStateType.LOBBY_COUNTDOWN;
            startCountdown();

        } else if (match.getCurrentState() == GameStateType.LOBBY_COUNTDOWN) {  //not enough players
            match.getState().current = GameStateType.WAITING_LOBBY;
            stopCountdown();
            minigame.readdPendingMatch(match);
        }
    }

    protected void startCountdown() {
        countdownShortened = false;
        
        PlayerUtils.clearInventory(players);
        items.giveItemsCountdown(players);
        
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
        items.giveItemsCountdownShortened(players);

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

        match.preloadGame(selectedMap, selectedTime, selectedWeather);

        if (sendMessage) {
            msgUtil.broadcastPresetPrefix("waiting-lobby.countdown-shortened");
        }
    }

    protected void stopCountdown() {
        if (timer != null) timer.stop();
        timer = null;
        bossBar.cancelCountdown();
        match.unloadGame();

        PlayerUtils.clearInventory(players);
        items.giveItemsWaitingPlayers(players);
    }


    //voting logic

    protected abstract void requireVotingMenu();

    //OVERRIDE if you need more voting options
    protected void prepareVoting() {
        if (mapVoting.getAvailableOptions().isEmpty()) {
            List<String> availableMaps = minigame.getAvailableMaps();
        
            while (availableMaps.size() > 3) {
                availableMaps.remove(
                    ThreadLocalRandom.current().nextInt(availableMaps.size())
                );
            }

            mapVoting.setAvailableOptions(availableMaps);
        }
    }

    //OVERRIDE if you need more voting options
    protected void finalizeVoting() {
        if (selectedMap != null) return;

        selectedMap = mapVoting.getMostVoted();

        if (selectedMap == null) {
            List<String> availableMaps = mapVoting.getAvailableOptions();
            if (!availableMaps.isEmpty()) {
                selectedMap = availableMaps.get(new Random().nextInt(availableMaps.size()));
            } else {
                BrlnsReb.getInstance().getLogger().error("CRITIC ERROR: No map enabled in config!");
                msgUtil.broadcastPrefix("§cError: no map available! Match cancelled.");
                match.forceStop();
                return;
            }
        }
    }


    //listeners access

    public abstract void onItemUse(CustomPlayer player, Item item);



    protected abstract WaitingLobbyItemManager requireItemManager();

    public Config getConfig() { return match.getConfig(); }
    public Config getMessages() { return match.getMessages(); }
    public String requireConfigPath() { return "waiting-lobby."; }
    public Messages getMsgUtil() { return msgUtil; }
    public Set<CustomPlayer> getPlayers() { return players; }
    public VotingSystem<String> getMapVoting() { return mapVoting; }
    public VotingSystem<TimeOfDay> getTimeVoting() { return timeVoting; }
    public VotingSystem<Weather> getWeatherVoting() { return weatherVoting; }
    
}