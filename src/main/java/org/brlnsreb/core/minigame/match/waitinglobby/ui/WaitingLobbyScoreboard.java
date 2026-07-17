package org.brlnsreb.core.minigame.match.waitinglobby.ui;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.abstraction.ScoreboardAbstract;

public class WaitingLobbyScoreboard extends ScoreboardAbstract {

    private final String minigameName;
    private final int matchId;

    public WaitingLobbyScoreboard(Match match) {
        this.minigameName = match.getMinigame().mgt.displayName;
        this.matchId = match.getId();
    }

    public void updateWaitingLobby(CustomPlayer player) {
        String[] lines = {
            "&a" + minigameName,
            "&b",
            "  &l&9Game ID",
            "   &e" + matchId
        };

        update(player, lines);
    }

}
