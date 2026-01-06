package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import com.brlnsreb.minigames.mm.config.MMConfig;

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
        nbt.putString("mm_projectile", "sword");
        nbt.putString("mm_thrower", murderer.getName());

        int cx = murderer.getFloorX() >> 4;
        int cz = murderer.getFloorZ() >> 4;
        
        EntitySnowball snowball = (EntitySnowball) Entity.createEntity("minecraft:snowball",
            level.getChunk(cx, cz),
            nbt
        );
        
        if (snowball == null) return;
        
        snowball.setNameTagVisible(false);

        snowball.shootingEntity = murderer;
        Vector3 direction = murderer.getDirectionVector();
        double speed = config.getSwordThrowSpeed();
        snowball.setMotion(direction.multiply(speed));
        
        snowball.spawnToAll();
    }
}