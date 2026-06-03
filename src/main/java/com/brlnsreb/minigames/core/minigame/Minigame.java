package com.brlnsreb.minigames.core.minigame;

import java.util.BitSet;
import java.util.HashSet;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.auth.AuthSystem;
import com.brlnsreb.minigames.core.minigame.match.MinigameMatch;
import com.brlnsreb.minigames.core.player.CustomPlayer;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public abstract class Minigame {
    
    protected final int id;
    protected final String nameTag;

    protected final MinigameCore plugin;
    protected Config config;
    protected Config messages;

    protected final MinigameLobby lobby;
    protected final HashSet<? extends MinigameMatch> matches;
    protected final BitSet busyMatchNumbers;
    protected MinigameMatch mainPendingMatch;
    protected int currentPlayers = 0;

    public Minigame(MinigameType minigame) {
        this.id = minigame.getId();
        this.nameTag = minigame.getNameTag();

        plugin = MinigameCore.getInstance();

        this.reloadConfig();
        this.lobby = createLobby();
        this.matches = new HashSet<>();
        this.busyMatchNumbers = new BitSet();
        this.mainPendingMatch = createMatch();
    }

    public boolean onLobbyJoin(Player player) {
        return lobby.onJoin(player);
    }

    public boolean onMatchJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;

        if (p.getPlayerData().name == null) {
            AuthSystem.openMenu(p);
            return false;
        }

        return mainPendingMatch.onJoin(player);
    }

    protected abstract MinigameLobby createLobby();
    public abstract MinigameMatch createMatch();

    protected void replaceMainPendingMatch(MinigameMatch match) {      //used in createMatch
        mainPendingMatch = match;
        lobby.onReplaceMainPendingMatch(match.getNumber());
    }

    protected int getMatchNumber() {                            //used in createMatch
        int n = busyMatchNumbers.nextClearBit(1);
        busyMatchNumbers.set(n);
        return n;
    }

    public void onMatchEnding(MinigameMatch match) {
        matches.remove(match);
        busyMatchNumbers.clear(match.getNumber());
    }

    public void reloadConfig() {
        this.config = new Config(plugin.getDataFolder() + this.nameTag + "/config.yml", Config.YAML);
        this.messages = new Config(plugin.getDataFolder() + this.nameTag + "/messages.yml", Config.YAML);

        lobby.reloadConfig();
        for (MinigameMatch match : matches) {
            match.reloadConfig();
        }
    }

    public int getPlayerCount() {
        int count = 0;

        count += lobby.getLevel().getPlayers().size();
        for (MinigameMatch match : matches) {
            count += match.getPlayers().size();
        }

        return count;
    }

    public HashSet<? extends MinigameMatch> getMatches() { return matches; }
    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
