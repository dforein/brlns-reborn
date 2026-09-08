package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.AxisAlignedBB;
import org.powernukkitx.math.BlockFace;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.TextFormat;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.maps.RandomSpawnsMap;
import org.brlnsreb.utils.Vect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class GoldSpawnMapper {

    private final Map<String, List<Vector3>> mapCache;
    private final File mapsFolder;
    private final Gson gson;
    
    private static final double MIN_SPACE = 1.8;
    private static final HashSet<String> SAFE_PASSABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Block.AIR,
            Block.TALL_GRASS, Block.TALL_DRY_GRASS,
            Block.SHORT_GRASS, Block.SHORT_DRY_GRASS,
            Block.DEADBUSH,
            Block.REEDS, Block.BAMBOO, 
            Block.KELP, Block.SEAGRASS,
            Block.CORAL_FAN_HANG, Block.CORAL_FAN_HANG2, Block.CORAL_FAN_HANG3, 
            Block.TUBE_CORAL, Block.BRAIN_CORAL, Block.BUBBLE_CORAL, Block.FIRE_CORAL, Block.HORN_CORAL,
            Block.TUBE_CORAL_FAN, Block.BRAIN_CORAL_FAN, Block.BUBBLE_CORAL_FAN, Block.FIRE_CORAL_FAN, Block.HORN_CORAL_FAN,
            Block.TUBE_CORAL_WALL_FAN, Block.BRAIN_CORAL_WALL_FAN, Block.BUBBLE_CORAL_WALL_FAN, Block.FIRE_CORAL_WALL_FAN, Block.HORN_CORAL_WALL_FAN,
            Block.DEAD_TUBE_CORAL, Block.DEAD_BRAIN_CORAL, Block.DEAD_BUBBLE_CORAL, Block.DEAD_FIRE_CORAL, Block.DEAD_HORN_CORAL,
            Block.DEAD_TUBE_CORAL_FAN, Block.DEAD_BRAIN_CORAL_FAN, Block.DEAD_BUBBLE_CORAL_FAN, Block.DEAD_FIRE_CORAL_FAN, Block.DEAD_HORN_CORAL_FAN,
            Block.DEAD_TUBE_CORAL_WALL_FAN, Block.DEAD_BRAIN_CORAL_WALL_FAN, Block.DEAD_BUBBLE_CORAL_WALL_FAN, 
            Block.DEAD_FIRE_CORAL_WALL_FAN, Block.DEAD_HORN_CORAL_WALL_FAN,
            Block.DANDELION, Block.POPPY, Block.BLUE_ORCHID, Block.ALLIUM, Block.AZURE_BLUET, Block.NETHER_SPROUTS,
            Block.RED_TULIP, Block.ORANGE_TULIP, Block.WHITE_TULIP, Block.PINK_TULIP, Block.OXEYE_DAISY, 
            Block.BROWN_MUSHROOM, Block.RED_MUSHROOM, Block.SUNFLOWER, Block.ROSE_BUSH, Block.PEONY, Block.LARGE_FERN, 
            Block.CORNFLOWER, Block.LILY_OF_THE_VALLEY, 
            Block.CRIMSON_FUNGUS, Block.CRIMSON_ROOTS, Block.WARPED_FUNGUS, Block.WARPED_ROOTS,
            Block.WATER, Block.FLOWING_WATER,
            Block.VINE, Block.CAVE_VINES, Block.WEEPING_VINES, Block.TWISTING_VINES,
            Block.WHEAT, Block.CARROTS, Block.POTATOES, Block.BEETROOT,
            Block.OAK_SAPLING, Block.BIRCH_SAPLING, Block.SPRUCE_SAPLING, Block.ACACIA_SAPLING,
            Block.CHERRY_SAPLING, Block.JUNGLE_SAPLING, Block.DARK_OAK_SAPLING, Block.PALE_OAK_SAPLING,
            Block.LADDER,
            Block.RAIL,
            Block.REDSTONE_WIRE, Block.TRIP_WIRE,
            Block.TORCH, Block.REDSTONE_TORCH,
            Block.SCAFFOLDING
        )
    );
    
    public GoldSpawnMapper() {
        this.mapCache = new HashMap<>();
        this.mapsFolder = new File(BrlnsReb.instance.getDataFolder(), "mm/maps");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
    }

    public void scanMap(RandomSpawnsMap map, String mapId, Player admin) {
        admin.sendMessage(TextFormat.YELLOW + "Starting scan for map: " + mapId);
        admin.sendMessage(TextFormat.GRAY + "This may take a while...");
        
        List<Vector3> validSpawns = new ArrayList<>();
        
        Level level = map.level;
        Vector3 min = map.min;
        Vector3 max = map.max;
        Vect curr = new Vect();
        
        int totalBlocks = (int)((max.x - min.x) * (max.y - min.y) * (max.z - min.z));
        int checked = 0;
        int lastPercent = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int x = (int)min.x; x <= max.x; x++) {
        for (int z = (int)min.z; z <= max.z; z++) {
        for (int y = (int)min.y; y <= max.y; y++) {

            ValidSpace space = getValidSpace(level, curr.set(x, y, z));

            if (space != null) {
                validSpawns.add(space.bottom);
            }
            
            checked++;
            
            int percent = (checked * 100) / totalBlocks;
            if (percent >= lastPercent + 5) {
                admin.sendMessage(TextFormat.GRAY + "Progress: " + percent + "% (" + validSpawns.size() + " spawns found)");
                lastPercent = percent;
            }

        }}}
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        saveToJson(mapId, validSpawns);
        mapCache.put(mapId, validSpawns);
        
        admin.sendMessage(TextFormat.GREEN + "Scan completed!");
        admin.sendMessage(TextFormat.GOLD + "Found: " + validSpawns.size() + " valid spawns");
        admin.sendMessage(TextFormat.GRAY + "Time: " + (elapsed / 1000.0) + "s");
        admin.sendMessage(TextFormat.GRAY + "Saved to: maps/" + mapId + ".json");
    }
    
    private ValidSpace getValidSpace(Level level, Vect pos) {
        Block blockTarget = pos.getBlock(level);
        Block blockAbove = pos.add(1.0, Vect.Y).getBlock(level);
        Block blockBelow = pos.add(-2.0, Vect.Y).getBlock(level);
        AxisAlignedBB targetBB = blockTarget.getBoundingBox();
        AxisAlignedBB belowBB = blockBelow.getBoundingBox();
        AxisAlignedBB aboveBB = blockAbove.getBoundingBox();
        boolean considerTarget = false;

        if (!SAFE_PASSABLE_BLOCKS.contains(blockTarget.getId())) return null;
        if (!blockBelow.isSolid(BlockFace.UP) && !blockBelow.isSolid(BlockFace.DOWN))
            if (blockTarget.isSolid(BlockFace.UP) || !blockTarget.isSolid(BlockFace.DOWN))
                return null;
            else
                considerTarget = true;

        Vector3 posBottom = pos.set(considerTarget ? targetBB.getMaxY() : belowBB.getMaxY(), Vect.Y).getNewVector3();
        Vector3 posTop;

        if (SAFE_PASSABLE_BLOCKS.contains(blockAbove.getId())) {
            posTop = null;
        } else {
            if (considerTarget)
                if (aboveBB.getMinY() - targetBB.getMaxY() >= MIN_SPACE)
                    posTop = pos.set(aboveBB.getMinY(), Vect.Y).getNewVector3();
                else
                    return null;
            else
                if (aboveBB.getMinY() - belowBB.getMaxY() >= MIN_SPACE)
                    posTop = pos.set(aboveBB.getMinY(), Vect.Y).getNewVector3();
                else
                    return null;
        }

        return new ValidSpace(posBottom, posTop);
    }
    
    public void removeVolume(String mapId, Vector3 pos1, Vector3 pos2, Player admin) {
        if (pos1 == null || pos2 == null) {
            admin.sendMessage(TextFormat.RED + "No positions saved");
            return;
        }

        List<Vector3> spawns = mapCache.get(mapId);
        
        if (spawns == null) {
            if (!loadFromJson(mapId)) {
                admin.sendMessage(TextFormat.RED + "Map not found: " + mapId);
                return;
                        }
            spawns = mapCache.get(mapId);
                }
                
        int beforeSize = spawns.size();
        
        double minX = Math.min(pos1.x, pos2.x);
        double maxX = Math.max(pos1.x, pos2.x);
        double minY = Math.min(pos1.y, pos2.y);
        double maxY = Math.max(pos1.y, pos2.y);
        double minZ = Math.min(pos1.z, pos2.z);
        double maxZ = Math.max(pos1.z, pos2.z);
        
        spawns.removeIf(v -> 
            v.x >= minX && v.x <= maxX &&
            v.y >= minY && v.y <= maxY &&
            v.z >= minZ && v.z <= maxZ
        );
        
        int removed = beforeSize - spawns.size();
        
        saveToJson(mapId, spawns);
        
        admin.sendMessage(TextFormat.GREEN + "Removed " + removed + " spawns from volume");
        admin.sendMessage(TextFormat.GRAY + "Remaining: " + spawns.size() + " spawns");
    }
    
    public void addVolume(String mapId, Vector3 pos1, Vector3 pos2, Level level, Player admin) {
        List<Vector3> spawns = mapCache.get(mapId);
        
        if (spawns == null) {
            if (!loadFromJson(mapId)) {
                spawns = new ArrayList<>();
                mapCache.put(mapId, spawns);
            } else {
                spawns = mapCache.get(mapId);
            }
        }
        
        int beforeSize = spawns.size();
        
        double minX = Math.min(pos1.x, pos2.x);
        double maxX = Math.max(pos1.x, pos2.x);
        double minY = Math.min(pos1.y, pos2.y);
        double maxY = Math.max(pos1.y, pos2.y);
        double minZ = Math.min(pos1.z, pos2.z);
        double maxZ = Math.max(pos1.z, pos2.z);
        Vect curr = new Vect();
        
        admin.sendMessage(TextFormat.YELLOW + "Scanning volume...");
        
        for (int x = (int)minX; x <= maxX; x++) {
        for (int z = (int)minZ; z <= maxZ; z++) {
        for (int y = (int)minY; y <= maxY; y++) {

            ValidSpace space = getValidSpace(level, curr.set(x, y, z));
            
            if (space != null && !spawns.contains(space.bottom)) {
                spawns.add(space.bottom);
            }

        }}}
        
        int added = spawns.size() - beforeSize;
        
        saveToJson(mapId, spawns);
        
        admin.sendMessage(TextFormat.GREEN + "Added " + added + " new spawns from volume");
        admin.sendMessage(TextFormat.GRAY + "Total: " + spawns.size() + " spawns");
    }
    
    public List<Vector3> getSpawns(String mapId) {
        if (mapCache.containsKey(mapId)) {
            return new ArrayList<>(mapCache.get(mapId));
        }

        if (loadFromJson(mapId)) {
            return new ArrayList<>(mapCache.get(mapId));
        }
        
        return new ArrayList<>();
    }
    
    public List<String> listMaps() {
        List<String> maps = new ArrayList<>();
        
        File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                maps.add(file.getName().replace(".json", ""));
            }
        }
        
        return maps;
    }
    
    private void saveToJson(String mapId, List<Vector3> spawns) {
        File file = new File(mapsFolder, mapId + ".json");
        
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, Object> data = new HashMap<>();
            data.put("map_name", mapId);
            data.put("spawn_count", spawns.size());
            data.put("valid_spawns", spawns);
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            BrlnsReb.logger.error("Failed to save map: " + mapId, e);
        }
    }
    
    private boolean loadFromJson(String mapId) {
        File file = new File(mapsFolder, mapId + ".json");
        
        if (!file.exists()) return false;
        
        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> data = gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
            
            List<Map<String, Double>> rawSpawns = (List<Map<String, Double>>) data.get("valid_spawns");
            List<Vector3> spawns = new ArrayList<>();
            
            for (Map<String, Double> coords : rawSpawns) {
                spawns.add(new Vector3(
                    coords.get("x"),
                    coords.get("y"),
                    coords.get("z")
                ));
            }
            
            mapCache.put(mapId, spawns);
            return true;
            
        } catch (IOException e) {
            BrlnsReb.logger.error("Failed to load map: " + mapId, e);
            return false;
        }
    }
    
    public boolean reloadMap(String mapId) {
        mapCache.remove(mapId);
        return loadFromJson(mapId);
    }

    public record MapInfo(String mapId, Vector3 min, Vector3 max, Level level) {};
    public record ValidSpace(Vector3 bottom, Vector3 top) {};

}