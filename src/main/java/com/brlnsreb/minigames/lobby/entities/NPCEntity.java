package com.brlnsreb.minigames.lobby.entities;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

public class NPCEntity extends EntityHuman implements CustomEntity {

    public static final String IDENTIFIER = "brlnsreb:npc";

    private static final double lookDistance = 10;
    private static final double distSqThreshold = lookDistance * lookDistance;
    private static final double rotationThreshold = 30;
    private double defaultYaw = 0;
    private double defaultPitch = 0;
    private double lastBodyYaw = 0;
    private double lerpSpeed = 0.25;

    private Runnable task = null;
    private EntityArmorStand label1 = null;
    private EntityArmorStand label2 = null;

    public NPCEntity(IChunk chunk, CompoundTag nbt) {
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
        this.setImmobile(false);
        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);
        
        this.setHealthCurrent(5);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByEntityEvent
            && ((EntityDamageByEntityEvent) source).getDamager() instanceof Player
            && task != null) {

                task.run();
                //Server.getInstance().getLogger().info("NPC: attack");
        }
        source.setCancelled(true);
    
        return false;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        if (task == null) return true;

        task.run();
        //Server.getInstance().getLogger().info("NPC: interact");
        return true;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) return false;

        if (currentTick % 2 == 0) {
            Player closest = null;
            double minDistanceSq = distSqThreshold;

            for (Player p : this.getLevel().getPlayers().values()) {
                double distSq = this.distanceSquared(p);
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closest = p;
                }
            }

            if (closest != null) {
                //players nearby
                double dx = closest.x - this.x;
                double dz = closest.z - this.z;
                double dy = (closest.y + closest.getEyeHeight()) - (this.y + this.getEyeHeight());
                double horizontalDist = Math.sqrt(dx * dx + dz * dz);

                double targetYaw = -Math.toDegrees(Math.atan2(dx, dz));
                double targetPitch = -Math.toDegrees(Math.atan2(dy, horizontalDist));

                double headDiff = targetYaw - this.headYaw;
                while (headDiff > 180) headDiff -= 360;
                while (headDiff < -180) headDiff += 360;
                double nextHeadYaw = this.headYaw + (headDiff * lerpSpeed);

                double angleDiff = targetYaw - lastBodyYaw;
                while (angleDiff > 180) angleDiff -= 360;
                while (angleDiff < -180) angleDiff += 360;

                if (Math.abs(angleDiff) > rotationThreshold) {
                    lastBodyYaw += angleDiff * lerpSpeed;
                }

                this.setRotation(lastBodyYaw, targetPitch, nextHeadYaw);
                this.updateMovement();

            } else {
                //no players
                if (Math.abs(this.yaw - defaultYaw) > 1 || Math.abs(this.pitch - defaultPitch) > 1) {
                    double yawDiff = defaultYaw - this.yaw;
                    while (yawDiff > 180) yawDiff -= 360;
                    while (yawDiff < -180) yawDiff += 360;
                    
                    double nextYaw = this.yaw + (yawDiff * lerpSpeed);
                    this.lastBodyYaw = nextYaw;

                    this.setRotation(nextYaw, defaultPitch, nextYaw);
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

    public void setDefaultPose(double yaw) {
        this.defaultYaw = yaw;
    }

    public void setDefaultPose(double yaw, double pitch) {
        this.defaultYaw = yaw;
        this.defaultPitch = pitch;
    }

    public void setLerpSpeed(double lerpSpeed) {
        this.lerpSpeed = lerpSpeed;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

    public void updateLabel(String label) {
        if (label1 == null) {
            createHologram(0.2);
        }

        label1.setNameTag(TextFormat.colorize(label));
    }

    public void updateLabel(String line1, String line2) {
        if (label1 == null || label2 == null) {
            createHologram(1.0);
            createHologram(0.5);
        }

        label1.setNameTag(TextFormat.colorize(line1));
        label2.setNameTag(TextFormat.colorize(line2));
    }

    private void createHologram(double verticalOffSet) {
        if (this.label1 != null && this.label2 != null) return;
        /*
        CompoundTag nbt = Entity.getDefaultNBT(this.getPosition().add(0, 1.0 + verticalOffSet, 0))
                            .putBoolean("Invisible", true)
                            .putBoolean("NoGravity", true)
                            .putBoolean("Invulnerable", true)
                            .putInt("DisabledSlots", 0x1F1F1F);

        EntityArmorStand hologram = new EntityArmorStand(this.chunk, nbt);

        hologram.setNameTagVisible(true);
        hologram.setNameTagAlwaysVisible(true);
        
        hologram.spawnToAll();*/

        if (this.label1 == null) {
            this.label1 = hologram;
        } else {
            this.label2 = hologram;
        }
    }

    public EntityArmorStand getLabel1() {
        return label1;
    }

    public EntityArmorStand getLabel2() {
        return label2;
    }

}
