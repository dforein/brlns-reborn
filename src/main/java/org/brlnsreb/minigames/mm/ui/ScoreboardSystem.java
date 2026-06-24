package org.brlnsreb.minigames.mm.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.roles.MMRole;
import org.brlnsreb.utils.abstraction.ScoreboardAbstract;

public class ScoreboardSystem extends ScoreboardAbstract {

    public void updateInGame(CustomPlayer player, String timer, int innocents, boolean sheriffAlive, MMRole role) {
        String[] lines = {
            "&a",
            "  &l&aInnocents:",
            "   &l&a" + innocents,
            "  &l&eSheriff:",
            "   &a" + (sheriffAlive ? "alive" : "dead"),
            "  &l&dGame time:",
            "   &a" + timer,
            "  &l&6Role:",
            "   " + getRoleText(role),
            "&b"
        };

        update(player, lines);
    }

    private String getRoleText(MMRole role) {
        switch (role) {
            case MURDERER:  return "&cmurderer";
            case SHERIFF:   return "&9sheriff";
            case INNOCENT:  return "&ainnocent";
            case SPECTATOR: return "&7spectator";
            default:        return "&cno role";
        }
    }
}
