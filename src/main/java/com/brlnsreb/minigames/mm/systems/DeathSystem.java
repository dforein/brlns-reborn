package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AnimateEntityPacket.Animation;
import cn.nukkit.utils.TextFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.mm.items.ItemManager;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.entities.DeadBodyEntity;

public class DeathSystem {
    
    private final MinigameCore plugin;
    private final MurderMysteryGame game;
    private static final List<String> REDSTONE_BLACKLIST = Arrays.asList(
        Block.TALL_GRASS, Block.TALL_DRY_GRASS,
        Block.SHORT_GRASS, Block.SHORT_DRY_GRASS,
        Block.WHITE_CARPET, Block.ORANGE_CARPET, Block.PURPLE_CARPET, Block.YELLOW_CARPET, 
        Block.MAGENTA_CARPET, Block.PALE_MOSS_CARPET, Block.LIGHT_BLUE_CARPET, Block.LIGHT_GRAY_CARPET,
        Block.DANDELION, Block.POPPY, Block.BLUE_ORCHID, Block.ALLIUM, Block.AZURE_BLUET, Block.NETHER_SPROUTS,
        Block.RED_TULIP, Block.ORANGE_TULIP, Block.WHITE_TULIP, Block.PINK_TULIP, Block.OXEYE_DAISY, 
        Block.BROWN_MUSHROOM, Block.RED_MUSHROOM, Block.SUNFLOWER, Block.ROSE_BUSH, Block.PEONY, Block.LARGE_FERN, 
        Block.CORNFLOWER, Block.LILY_OF_THE_VALLEY, Block.CRIMSON_FUNGUS, Block.WARPED_FUNGUS, Block.WARPED_ROOTS,
        Block.WATER, Block.FLOWING_WATER,
        Block.SNOW_LAYER,
        Block.VINE,
        Block.WHEAT, Block.CARROTS, Block.POTATOES, Block.BEETROOT,
        Block.OAK_SAPLING, Block.BIRCH_SAPLING, Block.SPRUCE_SAPLING, Block.ACACIA_SAPLING,
        Block.CHERRY_SAPLING, Block.JUNGLE_SAPLING, Block.DARK_OAK_SAPLING, Block.PALE_OAK_SAPLING,
        Block.LADDER
    );

    
    public DeathSystem(MinigameCore plugin, MurderMysteryGame game) {
        this.plugin = plugin;
        this.game = game;
    }
    
    public void kill(Player victim, boolean isSheriff) {
        MMConfig config = game.getConfig();
        
        game.getRoleManager().getGamePlayer(victim).setRole(MMRole.SPECTATOR);

        victim.setGamemode(Player.ADVENTURE);
        victim.noClip = false;
        victim.setFlying(true);
        victim.setAllowFlight(true);
        victim.setNameTagVisible(false);
        victim.setDataFlag(EntityFlag.INVISIBLE, true);
        victim.setDataFlag(EntityFlag.COLLIDABLE, false);

        ItemManager.clearInventory(victim);
        ItemManager.giveSpectatorItems(victim, config.getSpectatorItemName());
        plugin.getLogger().info("SPECTATOR ITEMS COMMAND PASSED");
        
        Position deathPos = victim.getPosition();

        int blindDuration = config.getDeathBlindness();
        Effect blindness = Effect.get(EffectType.BLINDNESS);
        blindness.setDuration(blindDuration * 20);
        blindness.setAmplifier(0);
        blindness.setVisible(false);
        victim.addEffect(blindness);

        victim.sendTitle(
            TextFormat.colorize(config.getMessageNoPrefix("dead-title")), 
            TextFormat.colorize(config.getMessageNoPrefix("dead-subtitle")),
            10, 60, 10
        );

        if (isSheriff) {
            plugin.getLogger().info("§a[DEBUG] Sheriff died, dropping hoe at " + deathPos);
            dropSheriffHoe(deathPos);
        }
        
        createBody(victim, deathPos);
        dropRedstone(deathPos);
    }

    public void dropSheriffHoe(Position pos) {
        MMConfig config = game.getConfig();

        try {
            Item hoe = Item.get(Item.GOLDEN_HOE, 0, 1);
            hoe.setCustomName(TextFormat.colorize(config.getSheriffHoeName()));
            
            CompoundTag nbt = Entity.getDefaultNBT(pos);
            nbt.putCompound("Item", NBTIO.putItemHelper(hoe));
            nbt.putBoolean("mm_sheriff_hoe", true);
            nbt.putShort("Health", 5);
            nbt.putShort("Age", -32768);

            int cx = pos.getFloorX() >> 4;
            int cz = pos.getFloorZ() >> 4;

            if (!pos.getLevel().isChunkLoaded(cx, cz)) {
                pos.getLevel().loadChunk(cx, cz);
            }

            EntityItem drop = (EntityItem) Entity.createEntity(
                Entity.ITEM,
                pos.getLevel().getChunk(cx, cz),
                nbt
            );
            
            if (drop != null) {
                drop.setNameTagVisible(true);
                drop.setNameTagAlwaysVisible(true);
                drop.setNameTag(TextFormat.colorize(config.getSheriffHoeName()));
                drop.setScale(1.2f);
                
                drop.spawnToAll();
                
                plugin.getLogger().info("§a[DEBUG] Sheriff hoe spawned successfully!");
            } else {
                plugin.getLogger().error("§c[ERROR] Failed to create sheriff hoe entity!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().error("§c[ERROR] Failed to spawn sheriff hoe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createBody(Player victim, Position pos) {
        pos = pos.add(0, -0.4, 0);

        int cx = pos.getFloorX() >> 4;
        int cz = pos.getFloorZ() >> 4;

        if (!pos.getLevel().isChunkLoaded(cx, cz)) {
            pos.getLevel().loadChunk(cx, cz);
        }

        IChunk chunk = (IChunk) pos.getLevel().getChunk(cx, cz);

        DeadBodyEntity body = new DeadBodyEntity(chunk, Entity.getDefaultNBT(pos));

        boolean fallForward = new java.util.Random().nextBoolean();
        Animation selectedAnimation = Animation.builder()
            .animation(fallForward ? "animation.corpse.fall_forward" : "animation.corpse.fall_backward") 
            .nextState(fallForward ? "animation.corpse.fall_forward" : "animation.corpse.fall_backward")
            .stopExpression("0")
            .stopExpressionVersion(16777216)
            .controller("__runtime_controller")
            .build();
        
        double yaw = victim.getYaw();
        double pitch = game.getConfig().getHeadPitchOffset();
        double headYaw = yaw + game.getConfig().getHeadYawOffset();
        
        body.setSkin(victim.getSkin());
        body.setRotation(yaw, pitch, headYaw);
        body.setDataFlag(EntityFlag.INVISIBLE, true);

        body.spawnToAll();

        plugin.getServer().getScheduler().scheduleDelayedTask(game.getPlugin(), () -> {
            body.setDataFlag(EntityFlag.INVISIBLE, false);
        }, 2);
        plugin.getServer().getScheduler().scheduleDelayedTask(game.getPlugin(), () -> {
            body.playAnimation(selectedAnimation);
        }, 3);

        game.getDeadBodies().add(body);
    }
    
    private void dropRedstone(Position pos) {
        MMConfig config = game.getConfig();

        int amount = config.getRedstoneDrop();
        java.util.Random random = new java.util.Random();
        
        List<Position> validPositions = new ArrayList<>();
        
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                Position checkPos = new Position(
                    pos.getFloorX() + offsetX,
                    pos.getFloorY(),
                    pos.getFloorZ() + offsetZ,
                    pos.getLevel()
                );
                
                while (checkPos.getLevelBlock().isAir() && checkPos.getY() > 0) {
                    checkPos.y--;
                }

                Block blockBelow = checkPos.getLevelBlock();

                checkPos.y++;
                Block targetBlock = checkPos.getLevelBlock();

                if (targetBlock.isAir() 
                    && blockBelow.isSolid() 
                    && !REDSTONE_BLACKLIST.contains(blockBelow.getId())) {
                    validPositions.add(checkPos);
                }
            }
        }
        
        int placed = 0;
        while (placed < amount && !validPositions.isEmpty()) {
            int index = random.nextInt(validPositions.size());
            Position targetPos = validPositions.remove(index);
            
            targetPos.getLevel().setBlock(targetPos, Block.get(Block.REDSTONE_WIRE));
            game.addTrackedRedstone(targetPos);

            placed++;
        }
    }
}