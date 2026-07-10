package org.brlnsreb.minigames.mm.entities;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;
import org.jetbrains.annotations.NotNull;

import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.custom.CustomEntity;
import org.powernukkitx.entity.custom.CustomEntityDefinition;
import org.powernukkitx.entity.projectile.EntityProjectile;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.nbt.tag.CompoundTag;

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
        this.setDataFlag(ActorFlags.SILENT, true);
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
