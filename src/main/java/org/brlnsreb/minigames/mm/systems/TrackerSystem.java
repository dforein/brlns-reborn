package org.brlnsreb.minigames.mm.systems;

import org.powernukkitx.Player;

import java.util.Collection;

import org.brlnsreb.minigames.mm.roles.GamePlayer;
import org.brlnsreb.minigames.mm.roles.MMRole;

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