package com.brlnsreb.minigames.lobby.entities;

import org.jetbrains.annotations.NotNull;

import com.brlnsreb.minigames.core.MinigameMatch;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class NPCEntity extends EntityHuman implements CustomEntity {

    public static final String IDENTIFIER = "brlnsreb:npc";
    private final double lookDistance = 8.0;
    private final double rotationThreshold = 45.0;
    private double lastBodyYaw = 0;

    private double minDistanceSq = lookDistance * lookDistance;
    //private final MinigameMatch minigame;
    //TODO: reset

    public NPCEntity(IChunk chunk, CompoundTag nbt){//, MinigameMatch minigame) {
        super(chunk, nbt);

        //this.minigame = minigame;
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
        this.setImmobile(false);
        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);
        
        this.setHealthCurrent(5);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) source).getDamager() instanceof Player) {
                //minigame.joinPlayer((Player) source.getEntity());
                Server.getInstance().getLogger().info("NPC: attack");
            }
        }
        source.setCancelled(true);
    
        return false;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        //minigame.joinPlayer(player);
        Server.getInstance().getLogger().info("NPC: interact");
        return true;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        if (currentTick % 2 == 0) {
            Player closest = null;

            for (Player p : this.getLevel().getPlayers().values()) {
                double distSq = this.distanceSquared(p);
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closest = p;
                }
            }

            if (closest != null) {
                double dx = closest.x - this.x;
                double dz = closest.z - this.z;
                double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));

                this.setHeadYaw(targetYaw);

                double angleDiff = targetYaw - lastBodyYaw;
                while (angleDiff > 180) angleDiff -= 360;
                while (angleDiff < -180) angleDiff += 360;

                if (Math.abs(angleDiff) > rotationThreshold) {
                    lastBodyYaw = targetYaw;

                    double dy = (closest.y + closest.getEyeHeight()) - (this.y + this.getEyeHeight());
                    double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                    double targetPitch = -Math.toDegrees(Math.atan2(dy, horizontalDist));

                    this.setRotation(lastBodyYaw, targetPitch);
                    this.updateMovement();
                }
            }
        }
        return super.onUpdate(currentTick);
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

}
