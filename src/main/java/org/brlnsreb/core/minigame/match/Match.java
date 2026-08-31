package org.brlnsreb.core.minigame.match;

import org.powernukkitx.event.player.PlayerDropItemEvent;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.maps.GameMapLevel;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.minigame.match.game.Game;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.brlnsreb.utils.messages.Messages;

public abstract class Match {
    
    protected final int id;
    protected final GameState state;
    protected final Set<CustomPlayer> players;
    protected final Set<CustomPlayer> spectators;
    protected final int number;
    protected final Minigame minigame;

    protected WaitingLobby waitingLobby;
    protected Game game = null;

    protected Config config;
    protected Config messages;
    protected final Messages msgUtil;
    protected Config mapSettings;

    public boolean closed = false;

    public Match(MinigameType minigame, int matchNumber) {
        this(MinigameManager.getMinigame(minigame), matchNumber);
    }
    
    public Match(Minigame minigame, int matchNumber) {
        this.id = ThreadLocalRandom.current().nextInt(10000000, 99999999);
        this.state = new GameState();
        this.players = new HashSet<>();
        this.spectators = new HashSet<>();
        this.number = matchNumber;
        this.minigame = minigame;

        this.config = getConfig();
        this.messages = getMessages();
        this.msgUtil = new Messages(this.messages, minigame.mgt.prefix, this.players, this.spectators);
        this.mapSettings = getMapSettings();

        this.waitingLobby = createWaitingLobby();
    }

    protected abstract WaitingLobby createWaitingLobby();
    protected abstract Game createGame(String map, TimeOfDay time, Weather weather);
    

    //join-leave logic

    public boolean onJoin(CustomPlayer player) {
        if (player.isTeleporting()) return false;

        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN:
                synchronized (players) {
                    return waitingLobby.onJoin(player);
                }
        
            default:
                onJoinAsSpectator(player);
                return true;
        }
    }

    public void onJoinAsSpectator(CustomPlayer player) {
        if (player.isTeleporting()) return;

        if (game != null) {
            game.onJoinAsSpectator(player);
            spectators.add(player);

            player.minigameCurrent = minigame;
            player.matchCurrent = this;
        }
    }

    public void onLeave(CustomPlayer player) {
        if (player.state == PlayerStateType.DEATH_LOBBY) return;

        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN -> {
                synchronized (players) { players.remove(player); }
                waitingLobby.onLeave(player);
                minigame.onMatchLeave();
            }
        
            case PREGAME_COUNTDOWN, IN_GAME, ENDING -> {
                if (player.isPlaying()) {
                    players.remove(player);
                    game.prepareAndSaveData(player, false);
                    game.onLeave(player);
                } else if (player.isGameSpectator()) {
                    spectators.remove(player);
                }
            }
        }
    }


    //game logic

    public void loadGame(String mapId, TimeOfDay time, Weather weather) {
        //used when the waiting lobby countdown is finishing
        if (game != null) return;
        game = createGame(mapId, time, weather);

        GameMapLevel map = game.getMap();
        msgUtil.broadcastPrefix(
            YamlUtil.getStr("match.waiting-lobby.going-to-play", Configs.getGlobalMessages()), 
            new String[] {
                YamlUtil.getStr(map.configPath + "name", mapSettings),
                map.time.displayName,
                map.weather.displayName
            }
        );
    }

    public void unloadGame() {
        //used when there aren't enough players anymore, but the game was already preloaded
        if (game != null) game.close();
        game = null;
    }

    public void onGameStart() {
        minigame.onReplacePendingMatch(this);

        try {
            game.onPregameStart();
        } catch (Exception e) {
            BrlnsReb.logger.error("Failed to start game, forcing match stop: " + e.getMessage(), e);
            this.forceStop();
            return;
        }

        waitingLobby.close();
        waitingLobby = null;
    }

    public void forceStop() {
        if (closed) return;
        
        msgUtil.broadcast(msgUtil.getStrPrefix(
            "match.force-stop", 
            Configs.getGlobalMessages()
        ));

        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN -> {
                for (CustomPlayer p : players) {
                    p.sendMessage(ChatMsgs.INFO_PFX + "§cThe server is forcing match stop, joining hub...");
                    waitingLobby.onLeave(p);
                    minigame.onLobbyJoin(p);
                }
                waitingLobby.close();
            }
        
            case PREGAME_COUNTDOWN, IN_GAME, ENDING -> {
                game.forceStop();
                for (CustomPlayer p : players) {
                    onLeave(p);
                    minigame.onLobbyJoin(p);
                }
                for (CustomPlayer s : spectators) {
                    minigame.onLobbyJoin(s);
                }
            }
        }

        closeMatch();
    }

    public void onEnding() {
        if (closed) return;

        for (CustomPlayer p : players) minigame.onLobbyJoin(p);
        for (CustomPlayer s : spectators) minigame.onLobbyJoin(s);

        closeMatch();
    }

    private void closeMatch() {
        players.clear();
        spectators.clear();

        minigame.onMatchEnding(this);
        closed = true;
    }


    //listener access

    public void onItemUse(CustomPlayer player, Item item) {
        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN -> waitingLobby.onItemUse(player, item);
            case PREGAME_COUNTDOWN, IN_GAME, ENDING -> game.onItemUse(player, item);
        }
    }

    public boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event) {
        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN: return waitingLobby.onItemHeld(player, event);
            case PREGAME_COUNTDOWN, IN_GAME, ENDING: return game.onItemHeld(player, event);
            default: return false;
        }
    }

    public boolean onItemDrop(CustomPlayer player, PlayerDropItemEvent event) {
        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN: return false;
            case PREGAME_COUNTDOWN, IN_GAME, ENDING: return game.onItemDrop(player, event);
            default: return false;
        }
    }


    public int getId() { return id; }
    public GameState getState() { return state; }
    public GameStateType state() { return state.current; }
    public Set<CustomPlayer> getPlayers() { return players; }
    public Set<CustomPlayer> getSpectators() { return spectators; }
    public int getNumber() { return number; }
    public WaitingLobby getWaitingLobby() { return waitingLobby; }
    public Game getGame() { return game; }
    public Minigame getMinigame() { return minigame; }
    public Config getConfig() { return minigame.getConfig(); }
    public Config getMessages() { return minigame.getMessages(); }
    public Config getMapSettings() { return minigame.getMapSettings(); }
    public Messages getMsgUtil() { return msgUtil; }
    
}