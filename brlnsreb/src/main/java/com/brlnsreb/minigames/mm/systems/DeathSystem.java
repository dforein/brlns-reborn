package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityDataTypes;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
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
        "minecraft:air",
        "minecraft:water",
        "minecraft:flowing_water",
        "minecraft:tallgrass",
        "minecraft:grass",
        "minecraft:double_plant",
        "minecraft:yellow_flower",
        "minecraft:red_flower",
        "minecraft:snow_layer",
        "minecraft:carpet",
        "minecraft:vine",
        "minecraft:wheat",
        "minecraft:sapling",
        "minecraft:web"
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
        
        Position deathPos = victim.getPosition();

        int blindDuration = config.getDeathBlindness();
        Effect blindness = Effect.get(EffectType.BLINDNESS);
        blindness.setDuration(blindDuration * 20);
        blindness.setAmplifier(0);
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
            //nbt.putShort("PickupDelay", Short.MAX_VALUE);     ///no pickup delay, new system
            nbt.putBoolean("mm_sheriff_hoe", true);

            int cx = pos.getFloorX() >> 4;
            int cz = pos.getFloorZ() >> 4;

            if (!pos.getLevel().isChunkLoaded(cx, cz)) {
                pos.getLevel().loadChunk(cx, cz);
            }

            EntityItem drop = (EntityItem) Entity.createEntity(
                "minecraft:item",
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
        IChunk chunk = (IChunk) pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4);
        DeadBodyEntity body = new DeadBodyEntity(chunk, Entity.getDefaultNBT(pos));
        
        body.setSkin(victim.getSkin());
        body.setRotation(game.getConfig().getHeadYawOffset(), game.getConfig().getHeadPitchOffset());
        body.spawnToAll();

        game.getDeadBodies().add(body);

        boolean fallsForward = new java.util.Random().nextBoolean();

        if (fallsForward) {
            //forward fall
            body.setDataFlag(EntityFlag.POWERED, true);
        } else {
            //backward fall
            body.setDataFlag(EntityFlag.SADDLED, true);
        }

        body.getEntityDataMap().put(EntityDataTypes.LAYING_AMOUNT, 1.0f);
        body.getEntityDataMap().put(EntityDataTypes.LAYING_AMOUNT_PREVIOUS, 1.0f);
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

                if (targetBlock.isAir() && 
                    blockBelow.isSolid() && 
                    !REDSTONE_BLACKLIST.contains(blockBelow.getId())) {
                    validPositions.add(checkPos);
                }
            }
        }
        
        int placed = 0;
        while (placed < amount && !validPositions.isEmpty()) {
            int index = random.nextInt(validPositions.size());
            Position targetPos = validPositions.remove(index);
            
            targetPos.getLevel().setBlock(targetPos, Block.get("minecraft:redstone_wire"));
            game.addTrackedRedstone(targetPos);

            placed++;
        }
    }
}