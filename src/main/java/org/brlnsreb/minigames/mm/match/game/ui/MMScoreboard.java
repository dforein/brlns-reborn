package org.brlnsreb.minigames.mm.match.game.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.utils.abstraction.ScoreboardAbstract;

public class MMScoreboard extends ScoreboardAbstract {

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
