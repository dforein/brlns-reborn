package org.brlnsreb.core.minigame.match;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.utils.Messages;

public abstract class MinigameMatch {
    
    protected final int id;
    protected final GameState state;
    protected final Set<Player> players;
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

        this.config = minigame.getConfig();
        this.messages = minigame.getMessages();
        this.msgUtil = new Messages(this.messages, this.players);

        this.waitingLobby = createWaitingLobby();
    }

    public void initGame(String selectedMap) {
        this.game = createGame(selectedMap);
    }

    public void startGame() {
        this.game.onGameStart();
    }

    protected abstract WaitingLobby createWaitingLobby();
    protected abstract MinigameGame createGame(String selectedMap);
    
    public boolean onJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;
        if (p.state == PlayerStateType.TELEPORTING) return false;

        switch (state.current) {
            case WAITING_LOBBY, LOBBY_COUNTDOWN:
                p.state = PlayerStateType.TELEPORTING;
                return waitingLobby.onJoin(player);
        
            default:
                return false;
        }
    }

    public boolean onJoinAsSpectator(Player player) {
        
    }

    public void onLeave(Player player) {
        players.remove(player);
        minigame.onLobbyJoin(player);
    }

    public abstract void onEnding();
    
    public int getMinPlayers() { return config.getInt("settings.min-players"); }
    public int getMaxPlayers() { return config.getInt("settings.max-players"); }

    public int getId() { return id; }
    public GameState getState() { return state; }
    public GameStateType getCurrentState() { return state.current; }
    public Set<Player> getPlayers() { return players; }
    public int getNumber() { return number; }
    public MinigameGame getGame() { return game; }
    public Minigame getMinigame() { return minigame; }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }
    public Messages getMsgUtil() { return msgUtil; }
    
}