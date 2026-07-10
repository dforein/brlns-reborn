package org.brlnsreb.minigames.mm.systems;

import org.brlnsreb.minigames.mm.config.MMConfig;
import org.brlnsreb.minigames.mm.entities.ThrownSwordEntity;

import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;

public class ProjectileSystem {
    
    private final MMConfig config;
    
    public ProjectileSystem(MMConfig config) {
        this.config = config;
    }
    
    public void throwSword(Player murderer) {
        Level level = murderer.getLevel();
        Vector3 eyePosition = new Vector3(
            murderer.x,
            murderer.y + murderer.getEyeHeight(),
            murderer.z
        );
        
        CompoundTag nbt = Entity.getDefaultNBT(eyePosition);
        nbt.putString("mm_thrower", murderer.getName());

        int cx = murderer.getFloorX() >> 4;
        int cz = murderer.getFloorZ() >> 4;

        if (!level.isChunkLoaded(cx, cz)) {
            level.loadChunk(cx, cz);
        }

        ThrownSwordEntity thrownSword = new ThrownSwordEntity(level.getChunk(cx, cz), nbt);

        thrownSword.shootingEntity = murderer;
        Vector3 direction = murderer.getDirectionVector();
        double speed = config.getSwordThrowSpeed() / 20;
        thrownSword.setMotion(direction.multiply(speed));
        
        thrownSword.spawnToAll();
    }
}