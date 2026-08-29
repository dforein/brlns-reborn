package org.brlnsreb.core.minigame;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.config.Configs;
import org.powernukkitx.utils.Config;

public abstract class Minigame {
    
    public final MinigameType mgt;

    protected final BrlnsReb plugin;
    protected Config config;
    protected Config messages;

    protected final MinigameLobby lobby;
    protected final HashSet<Match> matches;
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

        BrlnsReb.getScheduler().scheduleDelayedTask(BrlnsReb.instance, () -> createNewPendingMatch(), 6);
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
        if (result) lobby.updateJoinNpcSubtitle();

        return result;
    }

    public void onMatchLeave() {
        lobby.updateJoinNpcSubtitle();
    }


    //lobby and match management logic

    protected abstract MinigameLobby createLobby();
    protected abstract Match createMatch(int newMatchNumber);

    public boolean createNewPendingMatch() {
        if (!pendingMatches.isEmpty() && pendingMatches.element().getPlayers().size() < getMaxPlayers()) {
            return false;
        }

        Match match = createMatch(getNewMatchNumber());
        matches.add(match);
        pendingMatches.add(match);

        if (pendingMatches.size() < 2) {
            Match match2 = createMatch(getNewMatchNumber());
            matches.add(match2);
            pendingMatches.add(match2);
        }

        lobby.updateJoinNpcSubtitle();

        return true;
    }

    public boolean onReplacePendingMatch(Match match) {
        if (!pendingMatches.contains(match)) return false;
        if (getMainPendingMatch() != match) {
            return pendingMatches.remove(match);
        }

        pendingMatches.remove();
        createNewPendingMatch();

        lobby.updateJoinNpcSubtitle();

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

        count += lobby.getMap().getPlayers().size();
        for (Match match : matches) {
            count += match.getPlayers().size();
            count += match.getSpectators().size();
        }

        return count;
    }

    public int getMinPlayers() { return config.getInt("settings.min-players"); }
    public int getMaxPlayers() { return config.getInt("settings.max-players"); }
    public List<String> getAvailableMapIds() { return config.getStringList("map-settings.enabled-maps"); }

    public MinigameLobby getLobby() { return lobby; }
    public HashSet<Match> getMatches() { return matches; }
    public Match getMainPendingMatch() { return pendingMatches.isEmpty() ? null : pendingMatches.element(); }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
