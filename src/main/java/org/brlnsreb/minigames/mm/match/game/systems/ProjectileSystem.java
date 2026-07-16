package org.brlnsreb.minigames.mm.match.game.systems;

import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Sound;
import org.powernukkitx.math.Vector3;

public class ProjectileSystem {
    
    private final MMGame game;
    private static double swordThrowSpeed;
    
    public ProjectileSystem(MMGame game) {
        this.game = game;
        swordThrowSpeed = game.getConfig().getDouble("game.items.sword.throw-speed");
    }
    
    public void throwSword(Player murderer) {
        Level level = game.getArena().getLevel();
        Vector3 eyePosition = new Vector3(
            murderer.x,
            murderer.y + murderer.getEyeHeight(),
            murderer.z
        );

        ThrownSwordEntity thrownSword = new ThrownSwordEntity(
            level.getChunk(murderer.getFloorX() >> 4, murderer.getFloorZ() >> 4), 
            Entity.getDefaultNBT(eyePosition)
        );

        thrownSword.shootingEntity = murderer;
        Vector3 direction = murderer.getDirectionVector();
        thrownSword.setMotion(direction.multiply(swordThrowSpeed));
        
        thrownSword.spawnToAll();

        level.addSound(murderer, Sound.RANDOM_BOW, 0.8f, 0.5f);
    }
}