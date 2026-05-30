package com.brlnsreb.minigames.core.lobby.entities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.CustomEntityDefinition;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

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
    private HologramEntity text1 = null;
    private HologramEntity text2 = null;
    private double verticalOffset1 = 0.6;
    private double verticalOffset2 = 0.25;

    public NPCEntity(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public void updateTitle(String line) {
        if (text1 == null) {
            text1 = createHologram(verticalOffset1);
        }

        text1.setText(line);
    }

    public void updateSubTitle(String line) {
        if (text2 == null) {
            text2 = createHologram(verticalOffset1);
        }

        text2.setText(line);
    }

    public void updateText(String line1, String line2) {
        if (text1 == null || text2 == null) {
            createHologram(verticalOffset1);
            createHologram(verticalOffset2);
        }

        text1.setText(line1);
        text2.setText(line2);
    }

    private HologramEntity createHologram(double verticalOffset) {
        Position pos = new Position(
            this.x,
            this.y + 1.5 + verticalOffset,
            this.z,
            this.level
        );

        HologramEntity text = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        text.spawnToAll();

        return text;
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
            BufferedImage img = ImageIO.read(new File(
                MinigameCore.getInstance().getDataFolder(), 
                "skins/" + skinFileName + ".png"
            ));
            Skin skin = new Skin();
            skin.setSkinData(img);
            skin.setSkinId(skinFileName);
            return skin;
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

    public HologramEntity getText1() {
        return text1;
    }

    public HologramEntity getText2() {
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
        this.setDataFlag(EntityFlag.SILENT, true);
        this.setDataFlag(EntityFlag.COLLIDABLE, false);

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
