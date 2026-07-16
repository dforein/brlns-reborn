package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.Server;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.scheduler.Task;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.ItemHelper;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.game.arena.Arena;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GoldSystem {
    
    private final Config config;
    private final Arena arena;

    private Task spawnTask;
    private List<Vector3> validSpawns;
    
    public GoldSystem(Config config, Arena arena) {
        this.config = config;
        this.arena = arena;
    }
    
    public void startSpawning() {
        int minInterval = config.getInt("game.gold.spawn-interval-min");
        int maxInterval = config.getInt("game.gold.spawn-interval-max");
        
        scheduleNext(minInterval, maxInterval);
    }
    
    private void scheduleNext(int min, int max) {
        int delay = (min + ThreadLocalRandom.current().nextInt(max - min + 1)) * 20;
        
        spawnTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                spawnGold();
                scheduleNext(min, max);
            }
        };
        
        Server.getInstance().getScheduler().scheduleDelayedTask(BrlnsReb.instance, spawnTask, delay);
    }

    private void spawnGold() {
        if (validSpawns == null || validSpawns.isEmpty()) {
            BrlnsReb.instance.getLogger().warning("MM: No valid spawns available!");
            return;
        }
        
        Vector3 randomSpawn = validSpawns.get(ThreadLocalRandom.current().nextInt(validSpawns.size()));
        Position spawnPos = new Position(
            randomSpawn.x, 
            randomSpawn.y, 
            randomSpawn.z, 
            arena.getLevel()
        ).add(0.5, 0.5, 0.5);
        
        spawnGoldAt(spawnPos);
    }

    private void spawnGoldAt(Position pos) {
        Item gold = Item.get(Item.GOLD_INGOT, 0, 1);

        CompoundTag nbt = Entity.getDefaultNBT(pos);
        nbt.putCompound("Item", ItemHelper.write(gold));
        nbt.putBoolean("Mergeable", false);
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

    public void loadSpawns() {
        GoldSpawnMapper mapper = new GoldSpawnMapper();
        this.validSpawns = mapper.getSpawns(arena.getMapId());
        
        if (validSpawns.isEmpty()) {
            BrlnsReb.instance.getLogger().warning("MM: No gold spawns found for map: " + arena.getMapId());
        }
    }
}