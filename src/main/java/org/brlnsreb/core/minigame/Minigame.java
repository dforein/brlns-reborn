package org.brlnsreb.core.minigame;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.utils.Config;

public abstract class Minigame {
    
    public final MinigameType mgt;

    protected final BrlnsReb plugin;
    protected Config config;
    protected Config messages;

    protected final MinigameLobby lobby;
    protected final HashSet<? extends MinigameMatch> matches;
    protected final BitSet busyMatchNumbers;
    protected Queue<MinigameMatch> pendingMatches;

    public Minigame(MinigameType minigameType) {
        this.mgt = minigameType;

        plugin = BrlnsReb.getInstance();

        this.config = ConfigManager.getConfig(this.mgt.nameTag + "/config.yml");
        this.messages = ConfigManager.getConfig(this.mgt.nameTag + "/messages.yml");

        this.lobby = createLobby();
        this.matches = new HashSet<>();
        this.busyMatchNumbers = new BitSet();

        this.pendingMatches = new ArrayDeque<>();
        createNewPendingMatch();
    }

    public void onConfigReload() {
        lobby.onConfigReload();
    }


    //join logic

    public boolean onLobbyJoin(CustomPlayer player) {
        if (player.isTeleporting()) return false;
        
        return lobby.onJoin(player);
    }

    public boolean onMatchJoin(CustomPlayer player) {
        if (!player.getPlayerData().isLogged()) {
            AuthSystem.openMenu(player);
            return false;
        }

        return pendingMatches.element().onJoin(player);
    }


    //lobby and match management logic

    protected abstract MinigameLobby createLobby();
    protected abstract MinigameMatch createMatch(int newMatchNumber);

    public boolean createNewPendingMatch() {
        if (!pendingMatches.isEmpty()
            && pendingMatches.element().getPlayers().size() < getMaxPlayers()) {
                return false;
        }

        pendingMatches.add(createMatch(getNewMatchNumber()));
        if (pendingMatches.size() < 2) {
            pendingMatches.add(createMatch(getNewMatchNumber()));
        }

        lobby.onReplaceMainPendingMatch(pendingMatches.element().getNumber());

        return true;
    }

    public boolean onReplacePendingMatch(MinigameMatch match) {
        if (!pendingMatches.contains(match)) return false;

        if (!pendingMatches.isEmpty()
            && pendingMatches.element().getPlayers().size() < getMaxPlayers()) {
                return false;
        }

        pendingMatches.remove();
        createNewPendingMatch();

        return true;
    }

    public void readdPendingMatch(MinigameMatch match) {
        if (!pendingMatches.contains(match)) {
            pendingMatches.add(match);
        }
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
    public MinigameMatch getMainPendingMatch() { return pendingMatches.element(); }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
