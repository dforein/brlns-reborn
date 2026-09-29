package org.brlnsreb.core.lobby.entities;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;

import org.powernukkitx.Player;
import org.powernukkitx.entity.item.EntityArmorStand;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.utils.TextFormat;

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
        this.setDataFlag(ActorFlags.SILENT, true);
        this.setDataFlag(ActorFlags.COLLIDABLE, false);
        this.setDataFlag(ActorFlags.HAS_COLLISION, false);
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

    @Override
    protected boolean shouldStopMotionWhenImmobile() {
        return true;
    }

}
