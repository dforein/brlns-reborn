package org.brlnsreb.core.minigame;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;

import cn.nukkit.utils.Config;

public abstract class Minigame {
    
    protected final int id;
    protected final String nameTag;

    protected final BrlnsReb plugin;
    protected Config config;
    protected Config messages;

    protected final MinigameLobby lobby;
    protected final HashSet<? extends MinigameMatch> matches;
    protected final BitSet busyMatchNumbers;
    protected MinigameMatch mainPendingMatch;
    protected MinigameMatch secondPendingMatch;

    public Minigame(MinigameType minigame) {
        this.id = minigame.getId();
        this.nameTag = minigame.getNameTag();

        plugin = BrlnsReb.getInstance();

        this.config = ConfigManager.getConfig(this.nameTag + "/config.yml");
        this.messages = ConfigManager.getConfig(this.nameTag + "/messages.yml");

        this.lobby = createLobby();
        this.matches = new HashSet<>();
        this.busyMatchNumbers = new BitSet();

        this.secondPendingMatch = createMatch(getNewMatchNumber());
        onMatchCreation();
    }

    public boolean onLobbyJoin(CustomPlayer player) {
        if (player.isTeleporting()) return false;
        
        player.setTeleporting();
        return lobby.onJoin(player);
    }

    public boolean onMatchJoin(CustomPlayer player) {
        if (player.isTeleporting()) return false;

        if (!player.getPlayerData().isLogged()) {
            AuthSystem.openMenu(player);
            return false;
        }

        player.setTeleporting();
        return mainPendingMatch.onJoin(player);
    }

    protected abstract MinigameLobby createLobby();
    protected abstract MinigameMatch createMatch(int newMatchNumber);

    public boolean onMatchCreation() {
        if (mainPendingMatch != null
            && mainPendingMatch.getPlayers().size() < getMaxPlayers()) {
                return false;
        }

        mainPendingMatch = secondPendingMatch;
        lobby.onReplaceMainPendingMatch(mainPendingMatch.getNumber());
        secondPendingMatch = createMatch(getNewMatchNumber());

        return true;
    }

    private int getNewMatchNumber() {
        int n = busyMatchNumbers.nextClearBit(1);
        busyMatchNumbers.set(n);
        return n;
    }

    public void onMatchEnding(MinigameMatch match) {
        matches.remove(match);
        busyMatchNumbers.clear(match.getNumber());
    }

    public void reloadConfig() {
        lobby.reloadConfig();
    }

    public int getPlayerCount() {
        int count = 0;

        count += lobby.getLevel().getPlayers().size();
        for (MinigameMatch match : matches) {
            count += match.getPlayers().size();
        }

        return count;
    }

    public int getMinPlayers() { return config.getInt("settings.min-players"); }
    public int getMaxPlayers() { return config.getInt("settings.max-players"); }
    public List<String> getAvailableMaps() { return config.getStringList("map-settings.enabled-maps"); }

    public MinigameLobby getLobby() { return lobby; }
    public HashSet<? extends MinigameMatch> getMatches() { return matches; }
    public MinigameMatch getMainPendingMatch() { return mainPendingMatch; }
    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
