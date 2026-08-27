package org.brlnsreb.minigames.mm.match.game.systems;

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
import org.brlnsreb.core.maps.GameMapLevel;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GoldSystem {
    
    private final Config config;
    private final GameMapLevel map;

    private Task spawnTask;
    private List<Vector3> validSpawns;
    
    public GoldSystem(Config config, GameMapLevel map) {
        this.config = config;
        this.map = map;
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
        
        BrlnsReb.getScheduler().scheduleDelayedTask(BrlnsReb.instance, spawnTask, delay);
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
            map.level
        ).add(0.5, 0.5, 0.5);
        
        spawnGoldAt(spawnPos);
    }

    private void spawnGoldAt(Position pos) {
        Item gold = Item.get(Item.GOLD_INGOT, 0, 1);

        CompoundTag nbt = Entity.getDefaultNBT(pos);
        nbt.putCompound("Item", ItemHelper.write(gold));
        nbt.putBoolean("Mergeable", false);
        nbt.putShort("Health", 5);
        
        int cx = pos.getChunkX();
        int cz = pos.getChunkZ();
        
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
        this.validSpawns = mapper.getSpawns(map.mapId);
        
        if (validSpawns.isEmpty()) {
            BrlnsReb.instance.getLogger().warning("MM: No gold spawns found for map: " + map.mapId);
        }
    }
}