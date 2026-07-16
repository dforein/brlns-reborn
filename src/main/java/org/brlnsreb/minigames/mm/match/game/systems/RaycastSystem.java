package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.level.Level;
import org.powernukkitx.level.particle.DustParticle;
import org.powernukkitx.math.Vector3;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.utils.Cooldown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RaycastSystem {
    
    private final MMGame game;
    private final Cooldown cooldown;
    private static double maxDistance;
    private static double maxDistanceSq;
    private static double step;
    
    public RaycastSystem(MMGame game) {
        this.game = game;
        maxDistance = game.getConfig().getInt("game.items.hoe.raycast-max-distance");
        maxDistanceSq = Math.pow(maxDistance + 2, 2);
        step = game.getConfig().getInt("game.items.hoe.raycast-step");

        cooldown = Cooldown.seconds(game.getConfig().getDouble("game.items.hoe.cooldown"));
    }
    
    public CustomPlayer shoot(CustomPlayer shooter) {
        if (!cooldown.check(shooter)) return null;

        Level level = game.getArena().getLevel();

        Vector3 start = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        Vector3 direction = shooter.getDirectionVector();

        List<CustomPlayer> potentialTargets = new ArrayList<>(game.getPlayers());
        Iterator<CustomPlayer> iterator = potentialTargets.iterator();
        while (iterator.hasNext()) {
            CustomPlayer p = iterator.next();
            if (!p.isOnline() || p.equals(shooter) || p.distanceSquared(start) <= maxDistanceSq) {
                potentialTargets.remove(p);
            }
        }

        boolean particle = true;
        for (double d = 0; d < maxDistance; d += step) {
            Vector3 point = start.add(direction.multiply(d));
            
            if (particle) level.addParticle(new DustParticle(point, 255, 255, 255));
            particle = !particle;
            
            if (level.getBlock(point).isSolid()) break;
            
            for (CustomPlayer target : potentialTargets) {
                if (target.getBoundingBox().grow(0.1, 0.1, 0.1).isVectorInside(point)) {
                    return target;
                }
            }
        }
        
        return null;
    }
}