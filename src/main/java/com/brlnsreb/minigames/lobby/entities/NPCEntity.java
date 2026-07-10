package com.brlnsreb.minigames.lobby.entities;

import java.util.function.Consumer;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;
import org.jetbrains.annotations.NotNull;

import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.EntityHuman;
import org.powernukkitx.entity.custom.CustomEntity;
import org.powernukkitx.entity.custom.CustomEntityDefinition;
import org.powernukkitx.entity.data.human.Skin;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.level.particle.FloatingTextParticle;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.utils.TextFormat;

public class NPCEntity extends EntityHuman implements CustomEntity {

    public static final String IDENTIFIER = "brlnsreb:npc";
    private static final double lookDistance = 10;
    private static final double distSqThreshold = lookDistance * lookDistance;
    private static final double rotationThreshold = 30;
    private double defaultYaw = 0;
    private double defaultPitch = 0;
    private double lastBodyYaw = 0;
    private double lerpSpeed = 0.25;

    private Consumer<Player> task = null;
    private FloatingTextParticle text1 = null;
    private FloatingTextParticle text2 = null;
    private double verticalOffset1 = 0.6;
    private double verticalOffset2 = 0.25;

    public NPCEntity(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public void updateText(String line) {
        if (text1 == null) {
            createHologram(verticalOffset1, line);
        }

        text1.setTitle(TextFormat.colorize(line));
    }

    public void updateText(String line1, String line2) {
        if (text1 == null || text2 == null) {
            createHologram(verticalOffset1, line1);
            createHologram(verticalOffset2, line2);
            return;
        }

        text1.setTitle(TextFormat.colorize(line1));
        text2.setTitle(TextFormat.colorize(line2));
    }

    private void createHologram(double verticalOffset, String title) {
        if (this.text1 != null && this.text2 != null) return;
        
        Position pos = new Position(
            this.getX(),
            this.getY() + 2.25 + verticalOffset,
            this.getZ(),
            this.getLevel()
        );

        FloatingTextParticle text = new FloatingTextParticle(pos, title);
        this.getLevel().addParticle(text);

        if (this.text1 == null) {
            this.text1 = text;
        } else {
            this.text2 = text;
        }
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
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByEntityEvent
            && ((EntityDamageByEntityEvent) source).getDamager() instanceof Player
            && task != null) {

                task.accept((Player) ((EntityDamageByEntityEvent) source).getDamager());
                //Server.getInstance().getLogger().info("NPC: attack");
        }
        source.setCancelled(true);
    
        return false;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        if (task == null) return true;

        task.accept(player);
        //Server.getInstance().getLogger().info("NPC: interact");
        return true;
    }

    public void setSkin(String skinFileName) {
        this.setSkin(loadSkin(skinFileName));
    }

    public Skin loadSkin(String skinFileName) {
        try {
            //TODO: load npc skin
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public void setTask(Consumer<Player> task) {
        this.task = task;
    }

    public FloatingTextParticle getText1() {
        return text1;
    }

    public FloatingTextParticle getText2() {
        return text2;
    }

    public void setTextVerticalOffset(double offset) {
        this.verticalOffset1 = offset;
    }

    public void setTextVerticalOffset(double offsetText1, double offsetText2) {
        this.verticalOffset1 = offsetText1;
        this.verticalOffset2 = offsetText2;
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
        
        this.setFireImmune(true);
        this.setInvulnerable(true);
        this.setNameTagVisible(false);
        this.setImmobile(false);
        this.setDataFlag(ActorFlags.SILENT, true);
        this.setDataFlag(ActorFlags.COLLIDABLE, false);

        this.setHealthCurrent(5);
    }

    @Override
    public float getGravity() {
        return 0.0f;
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
