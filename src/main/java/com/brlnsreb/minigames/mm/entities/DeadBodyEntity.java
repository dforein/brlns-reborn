package com.brlnsreb.minigames.mm.entities;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class DeadBodyEntity extends EntityHuman implements CustomEntity {

    public static final String IDENTIFIER = "mm:dead_body";
    
    private boolean animationPlayed = false;
    
    public DeadBodyEntity(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override 
    public String getOriginalName() { 
        return "Dead Body"; 
    }

    public static CustomEntityDefinition definition() {
        return CustomEntityDefinition.simpleBuilder(IDENTIFIER)
                .eid(IDENTIFIER)
                .hasSpawnEgg(false)
                .isSummonable(true)
                .originalName("Dead Body")
                .maxHealth(5)
                .collisionBox(0.6f, 1.8f)
                .physics(false, false, false)
                .pushable(false, false)
                .isPersistent(true)
                .build();
    }
    
    @Override
    protected void initEntity() {
        super.initEntity();
        
        this.setImmobile(true);
        this.setNameTagVisible(false);
        this.setCanClimb(false);

        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.NO_AI, true);
        this.setDataFlag(EntityFlag.HAS_COLLISION, false);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);
        this.setDataFlag(EntityFlag.HAS_GRAVITY, false);
        this.setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED, false);
        
        this.setHealth(1);
        this.setMaxHealth(1);

        Effect resistance = Effect.get(EffectType.RESISTANCE);
        resistance.setDuration(9999999 * 20);
        resistance.setAmplifier(255);
        resistance.setVisible(false);
        this.addEffect(resistance);
    }

    public void playFallAnimation(boolean forward) {
        if (this.animationPlayed) return;
        
        if (forward) {
            this.setDataFlag(EntityFlag.PLAYING_DEAD, true);
            this.setDataFlag(EntityFlag.SADDLED, false);
        } else {
            this.setDataFlag(EntityFlag.PLAYING_DEAD, false);
            this.setDataFlag(EntityFlag.SADDLED, true);
        }

        for (Player viewer : this.getViewers().values()) {
            this.sendData(viewer);
        }
        
        this.animationPlayed = true;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        this.sendData(player);
    }
    
    @Override
    public void setRotation(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = yaw;     //allinea la testa al corpo
        this.scheduleUpdate();
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
    public boolean entityBaseTick(int tickDiff) {
        return super.entityBaseTick(tickDiff);
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
    
    @Override
    public void close() {
        super.close();
    }
}