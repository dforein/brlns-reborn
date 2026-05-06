package com.brlnsreb.minigames.core.minigame;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.brlnsreb.minigames.core.minigame.lobby.DeathLobby;
import com.brlnsreb.minigames.core.minigame.lobby.WaitingLobby;
import com.brlnsreb.minigames.utils.MessageUtil;

public abstract class MinigameMatch {
    
    protected final int id;
    protected final GameState state;
    protected final Set<Player> players;
    protected final int number;

    protected WaitingLobby waitingLobby;
    protected MinigameGame game;
    protected DeathLobby deathLobby;

    protected Config config;
    protected Config messages;
    protected final MessageUtil msgUtil;
    
    public MinigameMatch(Minigame minigame, int matchNumber) {
        this.id = ThreadLocalRandom.current().nextInt(10000000, 99999999);
        this.state = new GameState();
        this.players = new HashSet<>();
        this.number = matchNumber;

        this.config = minigame.getConfig();
        this.messages = minigame.getMessages();
        this.msgUtil = new MessageUtil(this.messages, this.players);
    }
    
    public abstract void onJoin(Player player);
    public abstract void onJoinAsSpectator(Player player);
    public abstract void onLeave(Player player);

    public abstract void onEnding();
    
    public abstract int getMinPlayers();
    public abstract int getMaxPlayers();
    
    public int getId() { return id; }
    public GameState getState() { return state; }
    public GameStateType getCurrentState() { return state.current; }
    public int getNumber() { return number; }
    public MinigameGame getGame() { return game; }
    
}