package org.brlnsreb.core.minigame.match;

import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.minigame.match.game.MinigameGame;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.Messages;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public abstract class MinigameMatch {
    
    protected final int id;
    protected final GameState state;
    protected final Set<CustomPlayer> players;
    protected final int number;
    protected final Minigame minigame;

    protected WaitingLobby waitingLobby;
    protected MinigameGame game;

    protected Config config;
    protected Config messages;
    protected final Messages msgUtil;

    public MinigameMatch(MinigameType minigame, int matchNumber) {
        this(MinigameManager.getMinigame(minigame), matchNumber);
    }
    
    public MinigameMatch(Minigame minigame, int matchNumber) {
        this.id = ThreadLocalRandom.current().nextInt(10000000, 99999999);
        this.state = new GameState();
        this.players = new HashSet<>();
        this.number = matchNumber;
        this.minigame = minigame;

        this.config = getConfig();
        this.messages = getMessages();
        this.msgUtil = new Messages(this.messages, this.players);

        this.waitingLobby = createWaitingLobby();
    }

    protected abstract WaitingLobby createWaitingLobby();
    protected abstract MinigameGame createGame(String map, TimeOfDay time, Weather weather);
    

    //join-leave logic

    public boolean onJoin(CustomPlayer player) {
        if (player.isTeleporting()) return false;

        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN:
                return waitingLobby.onJoin(player);
        
            default:
                onJoinAsSpectator(player);
                return true;
        }
    }

    public void onJoinAsSpectator(CustomPlayer player) {
        if (game != null) {
            game.onJoinAsSpectator(player);
            players.add(player);

            player.currentMinigame = minigame;
            player.setMatch(this);
        }
    }

    public void onLeave(CustomPlayer player) {
        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN:
                waitingLobby.onLeave(player);
                players.remove(player);
                break;
        
            case PREGAME_COUNTDOWN, IN_GAME, ENDING:
                game.onLeave(player);
                players.remove(player);
                break;
        }
        
        if (!player.isOnline()) return;
        minigame.onLobbyJoin(player);
    }


    //game logic

    public void preloadGame(String map, TimeOfDay time, Weather weather) {
        //used when the waiting lobby countdown is finishing
        game = createGame(map, time, weather);
    }

    public void unloadGame() {
        //used when there aren't enough players anymore, but the game was already preloaded
        if (game != null) game.close();
        game = null;
    }

    public void onGameStart() {
        waitingLobby.close();
        game.onPregameStart();
    }

    public void forceStop() {
        msgUtil.broadcast(msgUtil.getStrPrefix(
            "force-stop", 
            ConfigManager.getGlobalMessages()
        ));

        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN:
                for (CustomPlayer p : players) {
                    waitingLobby.onLeave(p);
                    minigame.onLobbyJoin(p);
                }
                waitingLobby.close();
                break;
        
            case PREGAME_COUNTDOWN, IN_GAME, ENDING:
                game.forceStop();
                for (CustomPlayer p : players) {
                    game.onLeave(p);
                    minigame.onLobbyJoin(p);
                }
                break;
        }
    }

    public void onEnding() {
        for (CustomPlayer p : players) {
            game.prepareAndSaveData(p);
            minigame.onLobbyJoin(p);
        }
    }


    //events from listeners

    public void onItemUse(CustomPlayer player, Item item) {
        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN:
                waitingLobby.onItemUse(player, item);
                break;
        
            case PREGAME_COUNTDOWN, IN_GAME, ENDING:
                game.onItemUse(player, item);
                break;
        }
    }


    public int getId() { return id; }
    public GameState getState() { return state; }
    public GameStateType getCurrentState() { return state.current; }
    public Set<CustomPlayer> getPlayers() { return players; }
    public int getNumber() { return number; }
    public MinigameGame getGame() { return game; }
    public Minigame getMinigame() { return minigame; }
    public Config getConfig() { return minigame.getConfig(); }
    public Config getMessages() { return minigame.getMessages(); }
    public Messages getMsgUtil() { return msgUtil; }
    
}