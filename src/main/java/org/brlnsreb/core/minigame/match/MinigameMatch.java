package org.brlnsreb.core.minigame.match;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.utils.Messages;

public abstract class MinigameMatch {
    
    protected final int id;
    protected final GameState state;
    protected final Set<Player> players;
    protected final int number;
    protected final Minigame minigame;

    protected WaitingLobby waitingLobby;
    protected MinigameGame game;
    protected EndLobby endLobby;

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

        createWaitingLobby("waiting-lobby");
        createEndLobby("end-lobby");
    }

    protected abstract void createWaitingLobby(String configPath);
    protected abstract void createGame();
    protected abstract void createEndLobby(String configPath);
    
    public abstract boolean onJoin(Player player);
    public abstract boolean onJoinAsSpectator(Player player);
    public abstract boolean onLeave(Player player);

    public abstract void onEnding();
    
    public int getMinPlayers() { return config.getInt("match.min-players"); }
    public int getMaxPlayers() { return config.getInt("match.max-players"); }

    public int getId() { return id; }
    public GameState getState() { return state; }
    public GameStateType getCurrentState() { return state.current; }
    public Set<Player> getPlayers() { return players; }
    public int getNumber() { return number; }
    public MinigameGame getGame() { return game; }
    public Minigame getMinigame() { return minigame; }
    
}