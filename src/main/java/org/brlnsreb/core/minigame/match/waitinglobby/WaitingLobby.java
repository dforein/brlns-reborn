package org.brlnsreb.core.minigame.match.waitinglobby;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.minigame.match.waitinglobby.items.WaitingLobbyItemManager;
import org.brlnsreb.core.minigame.match.waitinglobby.ui.WaitingLobbyBossBar;
import org.brlnsreb.core.minigame.match.waitinglobby.ui.WaitingLobbyScoreboard;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.SoundUtil;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.VotingSystem;
import org.brlnsreb.utils.voting.Weather;

import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Sound;
import org.powernukkitx.utils.Config;

public abstract class WaitingLobby extends Lobby {

    protected final Set<CustomPlayer> players;

    protected final int minPlayers;
    protected final int maxPlayers;

    protected final int secondsCountdown;
    protected final int secondsShortenedCountdown;
    protected boolean countdownShortened = false;

    protected final Messages msgUtil;
    protected final WaitingLobbyBossBar bossBar;
    protected final WaitingLobbyScoreboard scoreboard;
    protected final WaitingLobbyItemManager items;

    protected TimerSystem timer;
    protected VotingSystem<String> mapVoting;
    protected VotingSystem<TimeOfDay> timeVoting = null;
    protected VotingSystem<Weather> weatherVoting = null;

    protected String selectedMapId;
    protected TimeOfDay selectedTime = null;
    protected Weather selectedWeather = null;

    public WaitingLobby(Match match) {
        super(match);

        this.players = match.getPlayers();

        this.minPlayers = minigame.getMinPlayers();
        this.maxPlayers = minigame.getMaxPlayers();

        Config globalConfig = Configs.getGlobalConfig();
        this.secondsCountdown = globalConfig.getInt("match.waiting-lobby.bossbar.countdown-seconds");
        this.secondsShortenedCountdown = globalConfig.getInt("match.waiting-lobby.bossbar.shortened-countdown-seconds");

        spawnNpc(
            configPath() + "npc.leave.",
            player -> {
                match.onLeave(player);
                minigame.onLobbyJoin(player);
            }
        );

        this.msgUtil = match.getMsgUtil();
        this.bossBar = new WaitingLobbyBossBar(this, secondsCountdown);
        this.scoreboard = new WaitingLobbyScoreboard(match);
        this.items = requireItemManager();

        initVotingSystems();
        requireVotingMenu();
        prepareVoting();
    }

    public void forceStart() {
        msgUtil.broadcastPrefix("§aGame start was forced by an op!");

        finalizeVoting();
        stopCountdown();
        onGameStart();
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

        checkPlayerNumber(players.size());

        return true;
    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.WAITING_LOBBY; 
    }

    protected void onJoinMessages(CustomPlayer player) {
        Messages.sendActionBar(
            players, 
            "match.waiting-lobby.action-bar.on-join", 
            new Object[] {player.data.name, players.size(), maxPlayers},
            Configs.getGlobalMessages(),
            999999
        );
    }

    protected void onJoinUi(CustomPlayer player) {
        bossBar.updateWaitingLobbyBossBar();
        scoreboard.updateWaitingLobby(player);
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

    //OVERRIDE if you need more voting options
    public void onLeave(CustomPlayer player) {
        Messages.sendActionBar(
            players, 
            "action-bar.on-leave", 
            new Object[] {player.data.name, players.size(), maxPlayers},
            Configs.getGlobalMessages(),
            999999
        );

        mapVoting.removePlayerVote(player);
        checkPlayerNumber(players.size() - 1);
    }


    //countdown logic

    protected void checkPlayerNumber(int playerNumber) {
        if (playerNumber >= maxPlayers) {
            if (!countdownShortened) shortenCountdown(true);
            
        } else if (playerNumber >= minPlayers && !countdownShortened) {    //if the countdown is already shortened, it will stay shortened
            match.getState().current = GameStateType.LOBBY_COUNTDOWN;
            startCountdown();

        } else if (playerNumber < minPlayers && match.getCurrentState() == GameStateType.LOBBY_COUNTDOWN) {  //not enough players
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
        timer = new TimerSystem();
        timer.start(secondsCountdown, () -> {
            int remaining = timer.getSecondsRemaining();

            if (remaining == secondsShortenedCountdown) {
                shortenCountdown(false);
            }

            bossBar.updateWaitingLobbyBossBar(remaining);
        }, null);
    }

    protected void shortenCountdown(boolean sendMessage) {
        countdownShortened = true;
        finalizeVoting();

        PlayerUtils.clearInventory(players);
        items.giveItemsCountdownShortened(players);

        if (timer != null) timer.stop();
        timer = new TimerSystem();
        timer.start(secondsShortenedCountdown, () -> {
            int remaining = timer.getSecondsRemaining();

            bossBar.updateWaitingLobbyBossBar(remaining);
            
            float pitch = ThreadLocalRandom.current().nextFloat(0.9f, 1.01f);
            SoundUtil.sendSoundTo(players, Sound.RANDOM_CLICK.getSound(), 1.0f, pitch);
        }, this::onGameStart);

        if (sendMessage) {
            msgUtil.broadcastPrefix(YamlUtil.getStr("match.waiting-lobby.timer-shortened", Configs.getGlobalMessages()));
        }

        match.preloadGame(selectedMapId, selectedTime, selectedWeather);
    }

    protected void stopCountdown() {
        if (timer != null) timer.stop();
        timer = null;
        countdownShortened = false;

        bossBar.cancelCountdown();
        match.unloadGame();

        PlayerUtils.clearInventory(players);
        items.giveItemsWaitingPlayers(players);
    }


    //game start

    protected void onGameStart() {
        Messages.resetActionBar(players);

        for (CustomPlayer p : players) {
            PlayerUtils.resetUiAndInventories(p);
        }

        match.onGameStart();
    }


    //voting logic

    protected abstract void initVotingSystems();
    protected abstract void requireVotingMenu();

    //OVERRIDE if you need more voting options
    protected void prepareVoting() {
        if (mapVoting.getAvailableOptions() == null) {
            List<String> availableMapIds = minigame.getAvailableMapIds();
        
            while (availableMapIds.size() > 3) {
                availableMapIds.remove(
                    ThreadLocalRandom.current().nextInt(availableMapIds.size())
                );
            }

            mapVoting.setAvailableOptions(availableMapIds);
        }
    }

    //OVERRIDE if you need more voting options
    protected void finalizeVoting() {
        if (selectedMapId != null) return;

        selectedMapId = mapVoting.getMostVoted();

        if (selectedMapId == null) {
            List<String> availableMapIds = mapVoting.getAvailableOptions();
            if (!availableMapIds.isEmpty()) {
                selectedMapId = availableMapIds.get(new Random().nextInt(availableMapIds.size()));
            } else {
                BrlnsReb.instance.getLogger().error("CRITIC ERROR: No maps enabled in config!");
                msgUtil.broadcastPrefix(ChatMsgs.ERROR_PFX + "No maps available! Match cancelled.");
                match.forceStop();
                return;
            }
        }
    }


    //listeners access

    public abstract void onItemUse(CustomPlayer player, Item item);
    public abstract boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event);



    protected abstract WaitingLobbyItemManager requireItemManager();

    public Config getConfig() { return match.getConfig(); }
    public Config getMessages() { return match.getMessages(); }
    public String requireConfigPath() { return "waiting-lobby."; }
    public Messages getMsgUtil() { return msgUtil; }
    public int getMaxPlayers() { return maxPlayers; }
    public Set<CustomPlayer> getPlayers() { return players; }
    public VotingSystem<String> getMapVoting() { return mapVoting; }
    public VotingSystem<TimeOfDay> getTimeVoting() { return timeVoting; }
    public VotingSystem<Weather> getWeatherVoting() { return weatherVoting; }
    
}