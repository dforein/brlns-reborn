package org.brlnsreb.core.minigame.match.game;

import java.util.Set;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.match.game.items.SpectatorItemManager;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.GameState;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public abstract class Game {

    protected final Match match;
    protected final Minigame minigame;
    protected final GameState state;
    protected final Set<CustomPlayer> players;
    protected final Set<CustomPlayer> spectators;
    protected final Arena arena;

    protected final Config config;
    protected final Messages msgUtil;
    protected final SpectatorItemManager spectatorItems;

    protected TimerSystem timer;

    protected final ServerScheduler scheduler;

    public Game(Match match, String map, TimeOfDay time, Weather weather) {
        this.config = match.getConfig();

        this.match = match;
        this.minigame = match.getMinigame();
        this.state = match.getState();
        this.players = match.getPlayers();
        this.spectators = match.getSpectators();
        this.arena = prepareArena(map, time, weather);

        this.msgUtil = match.getMsgUtil();
        this.spectatorItems = new SpectatorItemManager();

        this.scheduler = Server.getInstance().getScheduler();
    }

    private Arena prepareArena(String map, TimeOfDay time, Weather weather) {
        return new Arena(
            config,
            "map-settings.maps." + map,
            "settings.",
            time, weather
        );
    }

    
    //join-leave logic

    public void onJoin(CustomPlayer player) {
        Position spawnPos = onJoinPosition(player);
        if (spawnPos == null) {
            player.sendMessage(ChatMsgs.ERROR_PFX + "Something went wrong with the match start, joining lobby...");  //TEXT
            match.onLeave(player);
            return;
        }

        PlayerUtils.changeWorld(player, spawnPos, false);

        player.state = PlayerStateType.PLAYING;
        prepareGameData(player);
        onJoinPreparePlayer(player);
        setPregameNameTag(player);
    }

    protected abstract Position onJoinPosition(CustomPlayer player);
    protected abstract void prepareGameData(CustomPlayer player);
    protected abstract void onJoinPreparePlayer(CustomPlayer player);
    protected abstract void setPregameNameTag(CustomPlayer player);       //name tag + chat name tag

    public void onJoinAsSpectator(CustomPlayer player) {
        PlayerUtils.changeWorld(player, onJoinPosition(player), false);

        if (player.state == PlayerStateType.PLAYING) {      //reset everything
            PlayerUtils.resetUiAndInventories(player);
            PlayerUtils.resetPlayer(player, Player.ADVENTURE, 20);
        }

        player.setGameSpectator();
        spectatorItems.giveTeleporter(player);
        spectatorItems.giveActions(player);

        onJoinPrepareSpectator(player);
    }

    protected abstract void onJoinPrepareSpectator(CustomPlayer player);

    public abstract void onLeave(CustomPlayer player);
    public abstract void prepareAndSaveData(CustomPlayer player);


    //<GAME LIFECYCLE>

    //pregame

    public void onPregameStart() {
        for (CustomPlayer p : players) {
            onJoin(p);
        }

        prepareGame();
        onPregameCountdown();
    }

    protected abstract void prepareGame();


    //pregame countdown

    protected void onPregameCountdown() {
        state.current = GameStateType.PREGAME_COUNTDOWN;

        Config globalConfig = ConfigManager.getGlobalConfig();
        int secondsCountdown = globalConfig.getInt("match.game.pregame-countdown-seconds");

        timer = new TimerSystem();
        timer.start(secondsCountdown, () -> {
            updatePregameScoreboards(timer.getFormattedTime());

            int secondsRemaining = timer.getSecondsRemaining();
            if (secondsRemaining <= 3) {
                for (CustomPlayer p : players) {
                    p.sendTitle(TextFormat.colorize("&l&a" + secondsRemaining), "", 4, 17, 4);
                }
            }
        }, this::onGameStart);
    }

    protected abstract void updatePregameScoreboards(String formattedTime);


    //ingame

    protected void onGameStart() {
        state.current = GameStateType.IN_GAME;
        for (CustomPlayer p : players) setGameNameTag(p);
        startGame();
    }
    
    protected abstract void setGameNameTag(CustomPlayer player);
    protected abstract void startGame();


    //ending

    protected void onGameEnding() {
        state.current = GameStateType.ENDING;
        
        for (CustomPlayer p : players) {
            prepareAndSaveData(p);
        }
        
        endGame();
    }

    protected abstract void endGame();
    public abstract boolean checkWinConditions();    //should be considered also the case where everyone left the game, so no winners


    //others

    public void close() {
        arena.close();
    }

    public abstract void forceStop();

    //</GAME LIFECYCLE>


    //events from listeners

    public abstract void onItemUse(CustomPlayer player, Item item);
    public abstract boolean onChat(CustomPlayer player, PlayerChatEvent event);
    public abstract boolean onCommandPreprocess(CustomPlayer player, PlayerCommandPreprocessEvent event);

}
