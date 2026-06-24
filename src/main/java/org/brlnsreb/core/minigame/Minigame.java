package org.brlnsreb.core.minigame;

import java.util.BitSet;
import java.util.HashSet;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;

import cn.nukkit.Player;
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
    protected int currentPlayers = 0;

    public Minigame(MinigameType minigame) {
        this.id = minigame.getId();
        this.nameTag = minigame.getNameTag();

        plugin = BrlnsReb.getInstance();

        this.config = new Config(plugin.getDataFolder() + this.nameTag + "/config.yml", Config.YAML);
        this.messages = new Config(plugin.getDataFolder() + this.nameTag + "/messages.yml", Config.YAML);

        this.lobby = createLobby();
        this.matches = new HashSet<>();
        this.busyMatchNumbers = new BitSet();

        onMatchCreation();
    }

    public boolean onLobbyJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;
        if (p.isTeleporting()) return false;
        
        p.setTeleporting();
        return lobby.onJoin(player);
    }

    public boolean onMatchJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;
        if (p.isTeleporting()) return false;

        if (!p.getPlayerData().isLogged()) {
            AuthSystem.openMenu(p);
            return false;
        }

        p.setTeleporting();
        return mainPendingMatch.onJoin(player);
    }

    protected abstract MinigameLobby createLobby();
    protected abstract MinigameMatch createMatch(int newMatchNumber);

    public boolean onMatchCreation() {
        if (mainPendingMatch != null
            && mainPendingMatch.getPlayers().size() < mainPendingMatch.getMaxPlayers()) {
                return false;
        }

        replaceMainPendingMatch(
            createMatch(getNewMatchNumber())
        );

        return true;
    }

    private void replaceMainPendingMatch(MinigameMatch match) {
        mainPendingMatch = match;
        lobby.onReplaceMainPendingMatch(match.getNumber());
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
        this.config.reload();
        this.messages.reload();
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

    public MinigameLobby getLobby() { return lobby; }
    public HashSet<? extends MinigameMatch> getMatches() { return matches; }
    public MinigameMatch getMainPendingMatch() { return mainPendingMatch; }
    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    public Config getConfig() { return config; }
    public Config getMessages() { return messages; }

}
