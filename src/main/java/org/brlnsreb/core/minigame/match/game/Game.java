package org.brlnsreb.core.minigame.match.game;

import java.util.List;
import java.util.Set;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.maps.MapLevel;
import org.brlnsreb.core.minigame.match.game.items.SpectatorItemManager;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.GameState;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerGameData;
import org.brlnsreb.core.player.data.database.AccountsManager;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.ChatMsgs.Alignment;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

import org.powernukkitx.Player;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.event.player.PlayerDropItemEvent;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Location;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public abstract class Game {

    protected final Config config;
    protected final Config messages;
    protected final Messages msgUtil;

    protected final Match match;
    protected final Minigame minigame;
    protected final GameState state;
    protected final Set<CustomPlayer> players;
    protected final Set<CustomPlayer> spectators;
    protected final MapLevel map;

    protected final SpectatorItemManager spectatorItems;

    protected TimerSystem timer;

    protected final ServerScheduler scheduler;

    public Game(Match match, String mapId, TimeOfDay time, Weather weather) {
        this.config = match.getConfig();
        this.messages = match.getMessages();
        this.msgUtil = match.getMsgUtil();

        this.match = match;
        this.minigame = match.getMinigame();
        this.state = match.getState();
        this.players = match.getPlayers();
        this.spectators = match.getSpectators();
        this.map = prepareMap(mapId, time, weather);
        
        this.spectatorItems = new SpectatorItemManager();

        this.scheduler = BrlnsReb.getScheduler();
    }

    protected abstract MapLevel prepareMap(String mapId, TimeOfDay time, Weather weather);
    
    //join-leave logic

    public void onJoin(CustomPlayer player) {
        Location spawnLoc = onJoinLocation(player);
        if (spawnLoc == null) {
            player.sendMessage(ChatMsgs.ERROR_PFX + "Something went wrong with the match start, joining lobby...");  //TEXT
            match.onLeave(player);
            return;
        }

        PlayerUtils.changeWorld(player, spawnLoc, false);
        player.removeAllEffects();

        player.state = PlayerStateType.PLAYING;
        prepareGameData(player);
        onJoinPreparePlayer(player);
        setPregameNameTag(player);
    }

    protected abstract Location onJoinLocation(CustomPlayer player);
    protected abstract void prepareGameData(CustomPlayer player);
    protected abstract void onJoinPreparePlayer(CustomPlayer player);
    protected abstract void setPregameNameTag(CustomPlayer player);       //name tag + chat name tag

    public void onJoinAsSpectator(CustomPlayer player) {
        PlayerUtils.changeWorld(player, onJoinLocation(player), false);

        if (player.isPlaying()) {      //reset everything
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

    public void prepareAndSaveData(CustomPlayer player) { prepareAndSaveData(player, true); }
    public void prepareAndSaveData(CustomPlayer player, boolean message) {
        AccountsManager.savePlayerData(player);

        int oldLevel = player.data.getFloorLevel();
        player.data.updateLevel();
        player.updateExp();

        if (!message) return;

        PlayerGameData gameData = getGameData(player);
        player.sendMessage(ChatMsgs.buildString(Alignment.CENTER, 
            "§l§3Reward Summary",
            "§6You earned §l§2" + gameData.getCoinsEarned() + "§r §6coins",
            "§2You earned §l§6" + gameData.getExpEarned() + "§r §2of experience",
            "§6You got §l§d" + 0 + " §c" + 0 + " §e" + 0 + " §9" + 0 + "§r §6gems",     //TODO: gems
            "§2Support us at:",
            "§6store.brlns.reb"
        ));
        
        if (oldLevel < player.data.getFloorLevel()) {
            player.sendMessage(ChatMsgs.INFO_PFX + "Congratulations! you are now on level §e" + player.data.getFloorLevel());    //TEXT
        }
    }


    //<GAME LIFECYCLE>

    //pregame

    public void onPregameStart() {
        for (CustomPlayer p : players) {
            onJoin(p);
        }

        //starting message
        msgUtil.broadcast(ChatMsgs.buildString(Alignment.CENTER, 
            ChatMsgs.BROKENLENS_GAMES,
            "",
            "§7- " + minigame.mgt.displayName + " §7-",
            "",
            "§7 Starting in 10 seconds..."
        ));

        //builders message
        List<String> builders = config.getStringList(map.getConfigPath() + "builders");
        if (!builders.isEmpty()) {
            String buildersStr = String.join("&7, &d", builders);
            
            String buildersTeam = YamlUtil.getStr(map.getConfigPath() + "build-team", config);
            if (buildersTeam != null && buildersTeam.length() > 0) buildersStr = buildersStr + " &7/ &d" + buildersTeam;

            String creditsMsg = YamlUtil.getStr("match.game.map-credits", Configs.getGlobalMessages()).formatted(buildersStr);
            msgUtil.broadcastPrefix(creditsMsg);
        }

        prepareGame();
        onPregameCountdown();
    }

    protected abstract void prepareGame();


    //pregame countdown

    protected void onPregameCountdown() {
        state.current = GameStateType.PREGAME_COUNTDOWN;

        Config globalConfig = Configs.getGlobalConfig();
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

        scheduler.scheduleDelayedTask(BrlnsReb.instance, () -> {
            match.onEnding();
            map.close();
        }, config.getInt("game.ending-duration") * 20);
    }

    protected abstract void endGame();
    public abstract boolean checkWinConditions();    //should be considered also the case where everyone left the game, so no winners


    //others

    public void close() {
        map.close();
    }

    public abstract void forceStop();

    //</GAME LIFECYCLE>


    //events from listeners

    public abstract void onItemUse(CustomPlayer player, Item item);
    public abstract boolean onItemPickup(CustomPlayer player, EntityItem itemEntity);
    public abstract boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event);
    public abstract boolean onItemDrop(CustomPlayer player, PlayerDropItemEvent event);
    public abstract void onPlayerDamage(CustomPlayer player, EntityDamageEvent event);
    public abstract void onProjectileHit(CustomPlayer player, ProjectileHitEvent event);
    public abstract boolean onChat(CustomPlayer player, PlayerChatEvent event);
    public abstract boolean onCommandPreprocess(CustomPlayer player, PlayerCommandPreprocessEvent event);



    protected abstract PlayerGameData getGameData(CustomPlayer player);
    public Set<CustomPlayer> getPlayers() { return players; }
    public Set<CustomPlayer> getSpectators() { return spectators; }
    public MapLevel getMap() { return map; }
    public Config getConfig() { return config; }
    public Messages getMsgUtil() { return msgUtil; }
    public GameState getState() { return state; }
    public GameStateType getCurrentState() { return state.current; }
    public TimerSystem getTimer() { return timer; }

}
