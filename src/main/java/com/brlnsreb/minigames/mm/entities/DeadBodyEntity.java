package com.brlnsreb.minigames.mm.entities;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AnimateEntityPacket.Animation;

public class DeadBodyEntity extends EntityHuman implements CustomEntity {

    public static final String IDENTIFIER = "mm:dead_body";
    private boolean fallForward;

    public DeadBodyEntity(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    public static CustomEntityDefinition definition() {
        return CustomEntityDefinition.simpleBuilder(IDENTIFIER)
                .eid(IDENTIFIER)
                .hasSpawnEgg(false)
                .isSummonable(true)
                .health(5)
                .physics(false, false, false)
                .pushable(false, false)
                .isPersistent(true)
                .build();
    }
    
    @Override
    protected void initEntity() {
        super.initEntity();
        
        this.invulnerable = true;
        this.fireProof = true;
        this.setNameTagVisible(false);
        this.setCanClimb(false);
        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);
        this.setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED, false);
        
        this.setHealthCurrent(5);
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        this.sendData(player);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        
        return super.onUpdate(currentTick);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled(true);
        return false;
    }
    
    @Override
    public float getGravity() {
        return 0;
    }
    
    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }
    
    @Override
    public boolean canBeMovedByCurrents() {
        return false;
    }

    public boolean getFallForward() {
        return fallForward;
    }

    public Animation getAnimation() {
        return Animation.builder()
            .animation(fallForward ? "animation.corpse.fall_forward" : "animation.corpse.fall_backward") 
            .nextState(fallForward ? "animation.corpse.fall_forward" : "animation.corpse.fall_backward")
            .stopExpression("0")
            .stopExpressionVersion(16777216)
            .controller("__runtime_controller")
            .build();
    }

    public Animation getStaticAnimation() {
        return Animation.builder()
            .animation(fallForward ? "animation.corpse.lying_forward" : "animation.corpse.lying_backward") 
            .nextState(fallForward ? "animation.corpse.lying_forward" : "animation.corpse.lying_backward")
            .stopExpression("0")
            .stopExpressionVersion(16777216)
            .controller("__runtime_controller")
            .build();
    }
    
    public void setFallForward(boolean fallForward) {
        this.fallForward = fallForward;
    }
    
}