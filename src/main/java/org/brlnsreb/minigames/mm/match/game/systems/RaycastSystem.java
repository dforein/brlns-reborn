package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.level.Level;
import org.powernukkitx.level.Sound;
import org.powernukkitx.level.particle.DustParticle;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.scheduler.ServerScheduler;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.utils.Cooldown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RaycastSystem {
    
    private final MMGame game;
    private final ServerScheduler scheduler;

    private static double maxDistance;
    private static double maxDistanceSq;
    private static double step;
    private final double cooldownTicks;
    private final Cooldown cooldown;
    
    public RaycastSystem(MMGame game, ServerScheduler scheduler) {
        this.game = game;
        this.scheduler = scheduler;

        maxDistance = game.getConfig().getInt("game.items.hoe.raycast-max-distance");
        maxDistanceSq = Math.pow(maxDistance + 2, 2);
        step = game.getConfig().getDouble("game.items.hoe.raycast-step");

        this.cooldownTicks = game.getConfig().getDouble("game.items.hoe.shoot-cooldown") * 20;
        this.cooldown = Cooldown.ticks(cooldownTicks);
    }
    
    public CustomPlayer shoot(CustomPlayer shooter) {
        if (!cooldown.check(shooter.getUniqueId())) return null;

        Level level = game.getMap().getLevel();

        level.addSound(shooter, Sound.RANDOM_FIZZ, 0.8f, 0.9f);

        Vector3 start = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        Vector3 direction = shooter.getDirectionVector();

        List<CustomPlayer> potentialTargets = new ArrayList<>(game.getPlayers());
        Iterator<CustomPlayer> iterator = potentialTargets.iterator();
        while (iterator.hasNext()) {
            CustomPlayer p = iterator.next();
            if (!p.isOnline() || p.equals(shooter) || p.distanceSquared(start) > maxDistanceSq) {
                iterator.remove();
            }
        }

        startXpBarRecharge(shooter);

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

    private void startXpBarRecharge(CustomPlayer shooter) {
        for (int i = 0; i <= cooldownTicks; i++) {
            final int tick = i;
            
            scheduler.scheduleDelayedTask(() -> {
                if (!shooter.isPlaying()) return;
                int progress = (int) (tick / cooldownTicks * 100.0);
                shooter.setExperience(progress, shooter.data.getFloorLevel());
            }, i);
        }
    }

    public void resetXpBarRecharge(CustomPlayer shooter) {
        shooter.setExperience(100, shooter.data.getFloorLevel());
    }

}