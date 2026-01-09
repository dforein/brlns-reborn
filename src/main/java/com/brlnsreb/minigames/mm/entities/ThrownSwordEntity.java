package com.brlnsreb.minigames.mm.entities;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class ThrownSwordEntity extends EntityProjectile implements CustomEntity {

    public static final String IDENTIFIER = "mm:thrown_sword";

    public ThrownSwordEntity(IChunk chunk, CompoundTag nbt) {
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
                .collisionBox(0.25f, 0.25f)
                .attack(0)
                .physics(true, true, true)
                .pushable(false, false)
                .isPersistent(false)
                .build();
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.setScale(0.6f);
        this.setNameTagVisible(false);
        this.setCanClimb(false);
        this.setDataFlag(EntityFlag.SILENT, true);
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        this.sendData(player);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        if (entity instanceof DeadBodyEntity || entity.getId() == shootingEntity.getId()) {
            return false;
        }

        return super.canCollideWith(entity);
    }

    @Override
    public float getGravity() {
        return 0.03f;
    }

    @Override
    public float getDrag() {
        return 0.01f;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        boolean hasUpdate = super.onUpdate(currentTick);

        if (this.isCollided || this.age > 1200) {
            this.close();
            return false;
        }

        return hasUpdate;
    }
}
