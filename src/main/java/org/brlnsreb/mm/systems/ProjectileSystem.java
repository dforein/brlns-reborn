package org.brlnsreb.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import org.brlnsreb.mm.config.MMConfig;
import org.brlnsreb.mm.entities.ThrownSwordEntity;

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