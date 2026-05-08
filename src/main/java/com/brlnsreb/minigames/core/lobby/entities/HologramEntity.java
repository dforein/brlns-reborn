package com.brlnsreb.minigames.core.lobby.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

public class HologramEntity extends EntityArmorStand {

    public void setText(String text) {
        this.setNameTag(TextFormat.colorize(text));
    }

    public HologramEntity(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
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
        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);
        this.setDataFlag(EntityFlag.HAS_COLLISION, false);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        return false;
    }

    @Override
    public boolean entityBaseTick() {
        return false;
    }

    @Override
    public boolean canCollide() {
        return false;
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
