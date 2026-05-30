package com.brlnsreb.minigames.core.minigame.match;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.minigame.MinigameType;
import com.brlnsreb.minigames.utils.MessageUtil;

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
    protected final MessageUtil msgUtil;

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
        this.msgUtil = new MessageUtil(this.messages, this.players);
    }

    public void reloadConfig() {
        this.config = minigame.getConfig();
        this.messages = minigame.getMessages();
        this.msgUtil.reloadConfig(messages);
    }
    
    public abstract boolean onJoin(Player player);
    public abstract boolean onJoinAsSpectator(Player player);
    public abstract boolean onLeave(Player player);

    public abstract void onEnding();
    
    public abstract int getMinPlayers();
    public abstract int getMaxPlayers();
    
    public int getId() { return id; }
    public GameState getState() { return state; }
    public GameStateType getCurrentState() { return state.current; }
    public int getNumber() { return number; }
    public MinigameGame getGame() { return game; }
    public Minigame getMinigame() { return minigame; }
    
}