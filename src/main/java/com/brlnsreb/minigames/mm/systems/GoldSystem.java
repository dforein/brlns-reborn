package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.Task;
import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.core.Arena;

import java.util.List;
import java.util.Random;

public class GoldSystem {
    
    private final MinigameCore plugin;
    private final MMConfig config;
    private final Random random;
    private Task spawnTask;
    private List<Vector3> validSpawns;
    
    public GoldSystem(MinigameCore plugin, MMConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
    }
    
    public void startSpawning(Arena arena) {
        cleanupGold(arena.getLevel());

        int minInterval = config.getGoldSpawnIntervalMin();
        int maxInterval = config.getGoldSpawnIntervalMax();
        
        scheduleNext(arena, minInterval, maxInterval);
    }
    
    private void scheduleNext(Arena arena, int min, int max) {
        int delay = (min + random.nextInt(max - min + 1)) * 20;
        
        spawnTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                spawnGold(arena);
                scheduleNext(arena, min, max);
            }
        };
        
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, spawnTask, delay);
    }

    private void spawnGold(Arena arena) {
        if (validSpawns == null || validSpawns.isEmpty()) {
            plugin.getLogger().warning("No valid spawns available!");
            return;
        }
        
        Vector3 randomSpawn = validSpawns.get(random.nextInt(validSpawns.size()));
        Position spawnPos = new Position(
            randomSpawn.x, 
            randomSpawn.y, 
            randomSpawn.z, 
            arena.getLevel())
            .add(0.5, 0.5, 0.5);
        
        spawnGoldAt(spawnPos);
    }

    private void spawnGoldAt(Position pos) {
        Item gold = Item.get(Item.GOLD_INGOT, 0, 1);

        CompoundTag nbt = Entity.getDefaultNBT(pos);
        nbt.putCompound("Item", NBTIO.putItemHelper(gold));
        nbt.putBoolean("mm_gold", true);
        nbt.putShort("Health", 5);
        
        int cx = pos.getFloorX() >> 4;
        int cz = pos.getFloorZ() >> 4;
        
        if (!pos.getLevel().isChunkLoaded(cx, cz)) {
            pos.getLevel().loadChunk(cx, cz);
        }
        
        EntityItem entity = (EntityItem) Entity.createEntity(
            Entity.ITEM, 
            pos.getLevel().getChunk(cx, cz), 
            nbt
        );

        
        if (entity != null) {
            entity.spawnToAll();
        }
    }
    
    public void stop() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
    }

    public void cleanupGold(Level level) {
        for (Entity entity : level.getEntities()) {
            if (entity instanceof EntityItem && entity.namedTag != null && entity.namedTag.getBoolean("mm_gold")) {
                entity.close();
            }
        }
    }

    public void loadSpawns(GoldSpawnMapper mapper, String mapName) {
        this.validSpawns = mapper.getSpawns(mapName);
        
        if (validSpawns.isEmpty()) {
            plugin.getLogger().warning("No gold spawns found for map: " + mapName);
        } else {
            plugin.getLogger().info("Loaded " + validSpawns.size() + " gold spawns for " + mapName);
        }
    }
}