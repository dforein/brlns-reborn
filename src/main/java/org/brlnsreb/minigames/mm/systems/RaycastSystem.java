package org.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.config.MMConfig;

import java.util.Collection;

public class RaycastSystem {
    
    private final MMConfig config;
    
    public RaycastSystem(MMConfig config) {
        this.config = config;
    }
    
    public Player shoot(Player shooter) {
        Level level = shooter.getLevel();
        Vector3 start = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        Vector3 direction = shooter.getDirectionVector();
        
        double maxDistance = config.getRaycastMaxDistance();
        double step = config.getRaycastStep();

        double maxDistSq = Math.pow(maxDistance + 2, 2);
        Collection<Player> potentialTargets = level.getPlayers().values().stream()
            .filter(p -> !p.equals(shooter) && p.isOnline() && p.distanceSquared(start) <= maxDistSq)
            .toList();

        int particleCounter = 0;
        for (double d = 0; d < maxDistance; d += step) {
            Vector3 point = start.add(direction.multiply(d));
            
            if (particleCounter % 2 == 0) {
                level.addParticle(new DustParticle(point, 255, 255, 255));
            }
            particleCounter++;
            
            if (level.getBlock(point).isSolid()) {
                break;
            }
            
            for (Player target : potentialTargets) {
                if (((CustomPlayer) target).isGameSpectator()) continue;

                if (target.getBoundingBox().grow(0.1, 0.1, 0.1).isVectorInside(point)) {
                    return target;
                }
            }
        }
        
        return null;
    }
}