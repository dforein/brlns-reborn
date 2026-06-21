package org.brlnsreb.mm.systems;

import cn.nukkit.Player;
import org.brlnsreb.mm.roles.GamePlayer;
import org.brlnsreb.mm.roles.MMRole;
import java.util.Collection;

public class TrackerSystem {

    public double getNearestDistance(Player murderer, Collection<GamePlayer> allPlayers) {
        double nearestDistance = Double.MAX_VALUE;
        for (GamePlayer gp : allPlayers) {
            if (gp.isAlive() && gp.getRole() != MMRole.MURDERER && gp.getRole() != MMRole.SPECTATOR) {
                double dist = murderer.distance(gp.getPlayer());
                if (dist < nearestDistance) {
                    nearestDistance = dist;
                }
            }
        }
        return nearestDistance;
    }
}