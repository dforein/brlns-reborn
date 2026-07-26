package org.brlnsreb.core.minigame;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.utils.Config;

public abstract class Minigame {
    
    public final MinigameType mgt;

    protected final BrlnsReb plugin;
    protected Config config;
    protected Config messages;

    protected final MinigameLobby lobby;
    protected final HashSet<? extends Match> matches;
    protected final BitSet busyMatchNumbers;
    protected Queue<Match> pendingMatches;

    public Minigame(MinigameType minigameType) {
        this.mgt = minigameType;

        plugin = BrlnsReb.instance;

        this.config = Configs.getConfig(this.mgt.nameTag + "/config.yml");
        this.messages = Configs.getConfig(this.mgt.nameTag + "/messages.yml");

        this.lobby = createLobby();
        this.matches = new HashSet<>();
        this.busyMatchNumbers = new BitSet();

        this.pendingMatches = new ArrayDeque<>();
        createNewPendingMatch();
    }

    public void onConfigReload() {
        lobby.onConfigReload();

        for (Match match : pendingMatches) match.forceStop();
        pendingMatches.clear();

        createNewPendingMatch();
    }


    //join logic

    public boolean onLobbyJoin(CustomPlayer player) {
        if (player.isTeleporting()) return false;
        
        return lobby.onJoin(player);
    }

    public boolean onMatchJoin(CustomPlayer player) {
        if (!player.data.isLogged()) {
            AuthSystem.openMenu(player);
            return false;
        }

        Match mainPendingMatch = getMainPendingMatch();
        if (mainPendingMatch == null) return false;

        boolean result = mainPendingMatch.onJoin(player);
        if (result) lobby.onMatchJoin();

        return result;
    }


    //lobby and match management logic

    protected abstract MinigameLobby createLobby();
    protected abstract Match createMatch(int newMatchNumber);

    public boolean createNewPendingMatch() {
        if (!pendingMatches.isEmpty()
            && pendingMatches.element().getPlayers().size() < getMaxPlayers()) {
                return false;
        }

        pendingMatches.add(createMatch(getNewMatchNumber()));
        if (pendingMatches.size() < 2) {
            pendingMatches.add(createMatch(getNewMatchNumber()));
        }

        lobby.onReplaceMainPendingMatch();

        return true;
    }

    public boolean onReplacePendingMatch(Match match) {
        if (getMainPendingMatch() != match) {
            return pendingMatches.remove(match);
        }

        if (!pendingMatches.isEmpty()
            && pendingMatches.element().getPlayers().size() < getMaxPlayers()) {
                return false;
        }

        pendingMatches.remove();
        createNewPendingMatch();

        return true;
    }

    public void readdPendingMatch(Match match) {
        if (!pendingMatches.contains(match)) {
            pendingMatches.add(match);
        }
    }

    private int getNewMatchNumber() {
        int n = busyMatchNumbers.nextClearBit(1);
        busyMatchNumbers.set(n);
        return n;
    }

    public void onMatchEnding(Match match) {
        matches.remove(match);
        busyMatchNumbers.clear(match.getNumber());
    }



    public int getPlayerCount() {
        int count = 0;

        count += lobby.getLevel().getPlayers().size();
        for (Match match : matches) {
            count += match.getPlayers().size();
        }

        return count;
    }

    public int getMinPlayers() { return config.getInt("settings.min-players"); }
    public int getMaxPlayers() { return config.getInt("settings.max-players"); }
    public List<String> getAvailableMapIds() { return config.getStringList("map-settings.enabled-maps"); }

    public MinigameLobby getLobby() { return lobby; }
    public HashSet<? extends Match> getMatches() { return matches; }
    public Match getMainPendingMatch() { return pendingMatches.isEmpty() ? null : pendingMatches.element(); }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
