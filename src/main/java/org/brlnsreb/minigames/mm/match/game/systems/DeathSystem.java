package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockRedstoneWire;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.ItemHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.utils.YamlUtil;

public class DeathSystem {
    
    private final MMGame game;
    private final ServerScheduler scheduler;
    
    private static double deadBodyPitch;
    private static double deadBodyHeadYaw;

    private static final HashSet<String> REDSTONE_BLACKLIST = new HashSet<>(Arrays.asList(
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
            Block.LADDER,
            Block.GRASS_PATH
        )
    );

    
    public DeathSystem(MMGame game, ServerScheduler scheduler) {
        this.game = game;
        this.scheduler = scheduler;

        deadBodyPitch = game.getConfig().getDouble("game.dead-body.head-pitch-offset");
        deadBodyHeadYaw = game.getConfig().getDouble("game.dead-body.head-yaw-offset");
    }
    
    public void onDeath(CustomPlayer victim, Position deathPos) {
        createBody(victim, deathPos);
        dropRedstone(deathPos);
    }
    
    private void createBody(Player victim, Position pos) {
        pos = pos.add(0, -0.4, 0);

        IChunk chunk = (IChunk) pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4);

        DeadBodyEntity body = new DeadBodyEntity(chunk, Entity.getDefaultNBT(pos));

        body.setFallForward(ThreadLocalRandom.current().nextBoolean());
        body.setSkin(victim.getSkin());
        body.setRotation(victim.getYaw(), deadBodyPitch, deadBodyHeadYaw);

        body.setDataFlag(ActorFlags.INVISIBLE, true);       //TODO: test whether cloudburst is affected by the old limitations or not
        body.spawnToAll();

        scheduler.scheduleDelayedTask(BrlnsReb.instance, 
            () -> body.setDataFlag(ActorFlags.INVISIBLE, false), 
            2);
        scheduler.scheduleDelayedTask(BrlnsReb.instance, 
            () -> body.playAnimation(body.getAnimation()), 
            3);
        scheduler.scheduleDelayedTask(BrlnsReb.instance, 
            () -> body.playAnimation(body.getStaticAnimation()),
            20);
        scheduler.scheduleDelayedTask(BrlnsReb.instance,
            () -> body.playAnimation(body.getStaticAnimation()), 
            60);
    }
    
    private void dropRedstone(Position pos) {
        List<Position> validPositions = new ArrayList<>();
        
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if ((i * i + j * j) != 1) continue;

                Position checkPos = new Position(
                    pos.getFloorX() + i,
                    pos.getFloorY(),
                    pos.getFloorZ() + j,
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
        
        for (Position targetPos : validPositions) {
            BlockRedstoneWire redstone = new BlockRedstoneWire();
            redstone.setRedStoneSignal(15);
            targetPos.getLevel().setBlock(targetPos, redstone, true, false);
        }
    }

    public void dropSheriffHoe(Position pos) {
        Item hoe = Item.get(Item.GOLDEN_HOE, 0, 1);
        hoe.setCustomName(YamlUtil.getStr("game.items.hoe.name", game.getConfig()));
        
        CompoundTag nbt = Entity.getDefaultNBT(pos);
        nbt.putCompound("Item", ItemHelper.write(hoe));
        nbt.putShort("Health", 5);
        nbt.putShort("Age", -32768);

        EntityItem drop = (EntityItem) Entity.createEntity(
            Entity.ITEM,
            pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4),
            nbt
        );
        
        if (drop != null) {
            drop.setNameTagVisible(true);
            drop.setNameTagAlwaysVisible(true);
            drop.setNameTag(YamlUtil.getStr("game.items.hoe.name", game.getConfig()));
            drop.setScale(1.2f);
            
            drop.spawnToAll();
        }
    }

    public void cleanupSheriffHoe() {
        for (Entity entity : game.getArena().getLevel().getEntities()) {
            if (entity instanceof EntityItem) {
                entity.close();
            }
        }
    }

}