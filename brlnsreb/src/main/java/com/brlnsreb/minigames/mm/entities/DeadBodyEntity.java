package com.brlnsreb.minigames.mm.entities;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class DeadBodyEntity extends EntityHuman {

    public static final int NETWORK_ID = 999;
    
    public DeadBodyEntity(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public String getIdentifier() {
        return "mm:dead_body";
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }
    
    @Override
    protected void initEntity() {
        super.initEntity();
        
        this.setImmobile(true);
        this.setNameTagVisible(false);
        this.setCanClimb(false);

        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.NO_AI, true);
        
        this.setHealth(1);
        this.setMaxHealth(1);
    }
    
    @Override
    public boolean onUpdate(int currentTick) {
        return super.onUpdate(currentTick);
    }
    
    @Override
    public void setRotation(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = yaw; // Allinea la testa al corpo
        this.scheduleUpdate();
    }
    
    @Override
    public boolean entityBaseTick(int tickDiff) {
        return false;
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