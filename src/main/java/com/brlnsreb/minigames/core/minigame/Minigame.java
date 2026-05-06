package com.brlnsreb.minigames.core.minigame;

import java.util.BitSet;
import java.util.HashSet;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public abstract class Minigame {
    
    protected final int id;
    protected final String nameTag;

    protected final MinigameCore plugin;
    protected Config config;
    protected Config messages;

    protected final HashSet<? extends MinigameMatch> matches;
    protected final BitSet busyMatchNumbers;
    protected MinigameMatch mainPendingMatch;
    protected int currentPlayers = 0;

    public Minigame(MinigameType minigame) {
        this.id = minigame.getId();
        this.nameTag = minigame.getNameTag();

        plugin = MinigameCore.getInstance();
        this.config = new Config(plugin.getDataFolder() + this.nameTag + "/config.yml", Config.YAML);
        this.messages = new Config(plugin.getDataFolder() + this.nameTag + "/messages.yml", Config.YAML);

        this.matches = new HashSet<>();
        this.busyMatchNumbers = new BitSet();
        createMatch();
    }

    public boolean onJoin(Player player) {
        
    }

    public abstract void createMatch();

    private int getMatchNumber() {                            //used in createMatch
        int n = busyMatchNumbers.nextClearBit(1);
        busyMatchNumbers.set(n);
        return n;
    }

    public void onMatchEnding(MinigameMatch match) {
        matches.remove(match);
        busyMatchNumbers.clear(match.getNumber());
    }

    public HashSet<? extends MinigameMatch> getMatches() { return matches; }
    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
