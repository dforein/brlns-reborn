package com.brlnsreb.minigames.core.lobby.entities;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

public class HologramEntity extends EntityArmorStand implements CustomEntity {
    
    public static final String IDENTIFIER = "brlnsreb:hologram";

    public void setText(String text) {
        this.setNameTag(TextFormat.colorize(text));
    }

    public HologramEntity(IChunk chunk, CompoundTag nbt) {
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
        
        this.setImmobile(true);
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.setScale(0.0001f);

        this.setFireImmune(true);
        this.setInvulnerable(true);
        this.setCanClimb(false);
        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);
        this.setDataFlag(EntityFlag.HAS_COLLISION, false);
        
        this.setHealthCurrent(5);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        return false;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setCancelled();
        return false;
    }

}
