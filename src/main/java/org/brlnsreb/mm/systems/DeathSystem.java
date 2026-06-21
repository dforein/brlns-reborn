package org.brlnsreb.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockRedstoneWire;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.MinigameCore;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.mm.roles.MMRole;
import org.brlnsreb.mm.MurderMysteryGame;
import org.brlnsreb.mm.config.MMConfig;
import org.brlnsreb.mm.entities.DeadBodyEntity;

public class DeathSystem {
    
    private final MinigameCore plugin;
    private final MurderMysteryGame game;
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

    
    public DeathSystem(MinigameCore plugin, MurderMysteryGame game) {
        this.plugin = plugin;
        this.game = game;
    }
    
    public void kill(Player victim, boolean isSheriff) {
        MMConfig config = game.getConfig();
        
        game.getRoleManager().getGamePlayer(victim).setRole(MMRole.SPECTATOR);

        ((CustomPlayer) victim).setGameSpectator(true);
        victim.setAllowFlight(true);
        victim.setFlying(true);

        ItemManager.giveSpectatorItems(victim, config.getSpectatorItemName());
        
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

        if (isSheriff) {dropSheriffHoe(deathPos);}
        
        createBody(victim, deathPos);
        dropRedstone(deathPos);
    }

    public void dropSheriffHoe(Position pos) {
        MMConfig config = game.getConfig();

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

        boolean fallForward = ThreadLocalRandom.current().nextBoolean();
        body.setFallForward(fallForward);
        
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
            body.playAnimation(body.getAnimation());
        }, 3);

        plugin.getServer().getScheduler().scheduleDelayedTask(game.getPlugin(), () -> {
            body.playAnimation(body.getStaticAnimation());
        }, 20);
        plugin.getServer().getScheduler().scheduleDelayedTask(game.getPlugin(), () -> {
            body.playAnimation(body.getStaticAnimation());
        }, 60);

        game.getDeadBodies().add(body);
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
        
        while (!validPositions.isEmpty()) {
            Position targetPos = validPositions.removeLast();

            BlockRedstoneWire redstone = new BlockRedstoneWire();
            redstone.setRedStoneSignal(15);
            targetPos.getLevel().setBlock(targetPos, redstone, true, false);
            
            game.addTrackedRedstone(targetPos);
        }
    }

    public void cleanupBodies(Set<Entity> deadBodies) {
        for (Entity body : deadBodies) {
            if (body != null && !body.isClosed()) {
                body.close();
            }
        }
        deadBodies.clear();
    }

    public void cleanupSheriffHoe(Level level) {
        for (Entity entity : level.getEntities()) {
            if (entity instanceof EntityItem && entity.namedTag != null && entity.namedTag.getBoolean("mm_sheriff_hoe")) {
                entity.close();
            }
        }
    }

    public void cleanupRedstone(List<Position> redstonePositions) {
        if (redstonePositions.isEmpty()) return;

        int removed = 0;
        for (Position pos : redstonePositions) {
            if (pos.getLevel() != null) {
                if (pos.getLevel().getBlock(pos) instanceof BlockRedstoneWire) {
                    pos.getLevel().setBlock(pos, Block.get(Block.AIR));
                    removed++;
                }
            }
        }

        plugin.getLogger().info("Removed " + removed + " redstone blocks");
        
        redstonePositions.clear();
    }
}