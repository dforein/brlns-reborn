package com.brlnsreb.minigames.mm.entities;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class ThrownSwordEntity extends EntityProjectile implements CustomEntity {       //will replace snowball

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
                .maxHealth(5)
                .attack(0)
                .physics(false, true, false)
                .pushable(false, false)
                .isPersistent(false)
                .build();
    }

    @Override
    protected void initEntity() {
        initEntity();

        this.setNameTagVisible(false);
        this.setCanClimb(false);
        this.setDataFlag(EntityFlag.SILENT, true);

        this.setHealth(5);
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        this.sendData(player);
    }

    //others later
}
