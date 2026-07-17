package org.brlnsreb.minigames.mm.match.game.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.utils.abstraction.ScoreboardAbstract;

public class MMScoreboard extends ScoreboardAbstract {

    private final MMGame game;

    public MMScoreboard(MMGame game) {
        this.game = game;
    }

    public void updateGameScoreboards() {
        int innocents = game.getPlayers().size();
        if (game.isMurdererAlive()) innocents--;
        if (game.isSheriffAlive()) innocents--;

        String formattedTime = game.getTimer().getFormattedTime();

        for (CustomPlayer p : game.getPlayers()) {
            updateIngame(p, innocents, game.isSheriffAlive(), formattedTime, game.getGameData(p).role);
        }

        for (CustomPlayer s : game.getSpectators()) {
            updateSpectator(s, innocents, game.isSheriffAlive(), formattedTime, game.getSpectators().size());
        }
    }

    public void updateIngame(CustomPlayer player, int innocents, boolean isSheriffAlive, String formattedTime, MMRole role) {
        String[] lines = {
            "§a",
            "  §aInnocents:",
            "   §l§a" + innocents,
            "  §l§eSheriff:",
            "   §a" + (isSheriffAlive ? "alive" : "dead"),
            "  §l§dGame time:",
            "   §a" + formattedTime,
            "  §l§6Role:",
            "   " + getRoleText(role),
            "§b"
        };

        update(player, lines);
    }
    
    private String getRoleText(MMRole role) {
        switch (role) {
            case MURDERER:  return "§cmurderer";
            case SHERIFF:   return "§9sheriff";
            case INNOCENT:  return "§ainnocent";
            default:        return "§cno role";
        }
    }

    public void updateSpectator(CustomPlayer player, int innocents, boolean isSheriffAlive, String formattedTime, int spectators) {
        String[] lines = {
            "§a",
            "  §aInnocents:",
            "   §a" + innocents,
            "  §l§eSheriff:",
            "   §a" + (isSheriffAlive ? "alive" : "dead"),
            "  §l§dGame time:",
            "   §a" + formattedTime,
            "  §l§4Spectators:",
            "   §a" + spectators,
            "§b"
        };

        update(player, lines);
    }

}
