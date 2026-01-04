package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.Arena;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class GoldSpawnMapper {
    
    private final MinigameCore plugin;
    private final Map<String, List<Vector3>> mapCache;
    private final Map<String, List<Vector3>> barrierCache;
    private final File mapsFolder;
    private final File barriersFolder;
    private final Gson gson;
    
    private static final List<String> SAFE_PASSABLE = Arrays.asList(
        "minecraft:air",
        "minecraft:tallgrass",
        "minecraft:grass",
        "minecraft:double_plant",
        "minecraft:yellow_flower",
        "minecraft:red_flower",
        "minecraft:carpet",
        "minecraft:water",
        "minecraft:flowing_water",
        "minecraft:snow_layer",
        "minecraft:vine",
        "minecraft:wheat",
        "minecraft:carrots",
        "minecraft:potatoes",
        "minecraft:beetroot",
        "minecraft:sapling"
    );
    
    public GoldSpawnMapper(MinigameCore plugin) {
        this.plugin = plugin;
        this.mapCache = new HashMap<>();
        this.barrierCache = new HashMap<>();
        this.mapsFolder = new File(plugin.getDataFolder(), "maps");
        this.barriersFolder = new File(plugin.getDataFolder(), "barriers");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        if (!barriersFolder.exists()) {
            barriersFolder.mkdirs();
        }
    }
    
    public void scanArena(Arena arena, String mapName, Player admin) {
        scanArena(arena, mapName, admin, false);
    }

    public void scanArena(Arena arena, String mapName, Player admin, boolean useBarrierWhitelist) {
        admin.sendMessage(TextFormat.YELLOW + "Starting scan for map: " + mapName);
        if (useBarrierWhitelist) {
            admin.sendMessage(TextFormat.GRAY + "Using barrier whitelist mode");
        }
        admin.sendMessage(TextFormat.GRAY + "This may take a while...");
        
        Set<Vector3> whitelistedBarriers = new HashSet<>();
        if (useBarrierWhitelist) {
            List<Vector3> barriers = getBarriers(mapName);
            if (barriers.isEmpty()) {
                admin.sendMessage(TextFormat.RED + "No barrier file found for " + mapName);
                admin.sendMessage(TextFormat.YELLOW + "Scanning without whitelist...");
            } else {
                whitelistedBarriers.addAll(barriers);
                admin.sendMessage(TextFormat.GRAY + "Loaded " + whitelistedBarriers.size() + " whitelisted barriers");
            }
        }
        
        List<Vector3> validSpawns = new ArrayList<>();
        
        Level level = arena.getLevel();
        Vector3 min = arena.getMin();
        Vector3 max = arena.getMax();
        
        int totalBlocks = (int)((max.x - min.x) * (max.y - min.y) * (max.z - min.z));
        int checked = 0;
        int lastPercent = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int x = (int)min.x; x <= max.x; x++) {
            for (int z = (int)min.z; z <= max.z; z++) {
                for (int y = (int)min.y; y <= max.y; y++) {
                    Position pos = new Position(x, y, z, level);
                    
                    if (isValidSpawn(level, pos, whitelistedBarriers)) {
                        validSpawns.add(new Vector3(x, y, z));
                    }
                    
                    checked++;
                    
                    int percent = (checked * 100) / totalBlocks;
                    if (percent >= lastPercent + 5) {
                        admin.sendMessage(TextFormat.GRAY + "Progress: " + percent + "% (" + validSpawns.size() + " spawns found)");
                        lastPercent = percent;
                    }
                }
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        saveToJson(mapName, validSpawns);
        mapCache.put(mapName, validSpawns);
        
        admin.sendMessage(TextFormat.GREEN + "Scan completed!");
        admin.sendMessage(TextFormat.GOLD + "Found: " + validSpawns.size() + " valid spawns");
        admin.sendMessage(TextFormat.GRAY + "Time: " + (elapsed / 1000.0) + "s");
        admin.sendMessage(TextFormat.GRAY + "Saved to: maps/" + mapName + ".json");
    }
    
    public void scanForBarriers(Arena arena, String mapName, Player admin) {
        admin.sendMessage(TextFormat.YELLOW + "Starting barrier scan for map: " + mapName);
        admin.sendMessage(TextFormat.GRAY + "This may take a while...");
        
        List<Vector3> barriers = new ArrayList<>();
        
        Level level = arena.getLevel();
        Vector3 min = arena.getMin();
        Vector3 max = arena.getMax();
        
        int totalBlocks = (int)((max.x - min.x) * (max.y - min.y) * (max.z - min.z));
        int checked = 0;
        int lastPercent = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int x = (int)min.x; x <= max.x; x++) {
            for (int z = (int)min.z; z <= max.z; z++) {
                for (int y = (int)min.y; y <= max.y; y++) {
                    Position pos = new Position(x, y, z, level);
                    Block block = level.getBlock(pos);
                    
                    if (block.getId().equals("minecraft:barrier")) {
                        barriers.add(new Vector3(x, y, z));
                    }
                    
                    checked++;
                    
                    int percent = (checked * 100) / totalBlocks;
                    if (percent >= lastPercent + 5) {
                        admin.sendMessage(TextFormat.GRAY + "Progress: " + percent + "% (" + barriers.size() + " barriers found)");
                        lastPercent = percent;
                    }
                }
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        
        saveBarriersToJson(mapName, barriers);
        barrierCache.put(mapName, barriers);
        
        admin.sendMessage(TextFormat.GREEN + "Barrier scan completed!");
        admin.sendMessage(TextFormat.GOLD + "Found: " + barriers.size() + " barriers");
        admin.sendMessage(TextFormat.GRAY + "Time: " + (elapsed / 1000.0) + "s");
        admin.sendMessage(TextFormat.GRAY + "Saved to: barriers/" + mapName + ".json");
    }
    
    public void countBarriers(Arena arena, Player admin) {
        admin.sendMessage(TextFormat.YELLOW + "Counting barriers in arena...");
        
        Level level = arena.getLevel();
        Vector3 min = arena.getMin();
        Vector3 max = arena.getMax();
        
        int barrierCount = 0;
        
        for (int x = (int)min.x; x <= max.x; x++) {
            for (int z = (int)min.z; z <= max.z; z++) {
                for (int y = (int)min.y; y <= max.y; y++) {
                    Position pos = new Position(x, y, z, level);
                    Block block = level.getBlock(pos);
                    
                    if (block.getId().equals("minecraft:barrier")) {
                        barrierCount++;
                    }
                }
            }
        }
        
        admin.sendMessage(TextFormat.GREEN + "Barrier count: " + TextFormat.GOLD + barrierCount);
    }
    
    private boolean isValidSpawn(Level level, Position pos) {
        return isValidSpawn(level, pos, new HashSet<>());
    }

    private boolean isValidSpawn(Level level, Position pos, Set<Vector3> whitelistedBarriers) {
        Block blockTarget = level.getBlock(pos);
        Block blockBelow = level.getBlock(pos.down());
        Block blockAbove = level.getBlock(pos.up());
        
        String blockTargetId = blockTarget.getId();
        String blockAboveId = blockAbove.getId();
        String blockBelowId = blockBelow.getId();
        
        //barrier whitelist check
        Vector3 belowVec = new Vector3(pos.getFloorX(), pos.getFloorY() - 1, pos.getFloorZ());
        boolean isBelowWhitelisted = whitelistedBarriers.contains(belowVec);
        
        boolean isBelowValid;
        if (isBelowWhitelisted && blockBelowId.equals("minecraft:barrier")) {
            isBelowValid = true;
        } else {
            isBelowValid = blockBelow.isSolid() && !blockBelowId.equals("minecraft:barrier");
        }
        
        // Conditions: passable block, valid below, passable above
        return SAFE_PASSABLE.contains(blockTargetId) &&
            isBelowValid &&
            SAFE_PASSABLE.contains(blockAboveId);
    }
    
    public void removeVolume(String mapName, Vector3 pos1, Vector3 pos2, Player admin) {
        List<Vector3> spawns = mapCache.get(mapName);
        
        if (spawns == null) {
            if (!loadFromJson(mapName)) {
                admin.sendMessage(TextFormat.RED + "Map not found: " + mapName);
                return;
            }
            spawns = mapCache.get(mapName);
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
        
        saveToJson(mapName, spawns);
        
        admin.sendMessage(TextFormat.GREEN + "Removed " + removed + " spawns from volume");
        admin.sendMessage(TextFormat.GRAY + "Remaining: " + spawns.size() + " spawns");
    }
    
    public void addVolume(String mapName, Vector3 pos1, Vector3 pos2, Level level, Player admin) {
        List<Vector3> spawns = mapCache.get(mapName);
        
        if (spawns == null) {
            if (!loadFromJson(mapName)) {
                spawns = new ArrayList<>();
                mapCache.put(mapName, spawns);
            } else {
                spawns = mapCache.get(mapName);
            }
        }
        
        int beforeSize = spawns.size();
        
        double minX = Math.min(pos1.x, pos2.x);
        double maxX = Math.max(pos1.x, pos2.x);
        double minY = Math.min(pos1.y, pos2.y);
        double maxY = Math.max(pos1.y, pos2.y);
        double minZ = Math.min(pos1.z, pos2.z);
        double maxZ = Math.max(pos1.z, pos2.z);
        
        admin.sendMessage(TextFormat.YELLOW + "Scanning volume...");
        
        for (int x = (int)minX; x <= maxX; x++) {
            for (int z = (int)minZ; z <= maxZ; z++) {
                for (int y = (int)minY; y <= maxY; y++) {
                    Position pos = new Position(x, y, z, level);
                    Vector3 vec = new Vector3(x, y, z);
                    
                    if (isValidSpawn(level, pos) && !spawns.contains(vec)) {
                        spawns.add(vec);
                    }
                }
            }
        }
        
        int added = spawns.size() - beforeSize;
        
        saveToJson(mapName, spawns);
        
        admin.sendMessage(TextFormat.GREEN + "Added " + added + " new spawns from volume");
        admin.sendMessage(TextFormat.GRAY + "Total: " + spawns.size() + " spawns");
    }
    
    public List<Vector3> getSpawns(String mapName) {
        if (mapCache.containsKey(mapName)) {
            return new ArrayList<>(mapCache.get(mapName));
        }

        if (loadFromJson(mapName)) {
            return new ArrayList<>(mapCache.get(mapName));
        }
        
        return new ArrayList<>();
    }
    
    public List<Vector3> getBarriers(String mapName) {
        if (barrierCache.containsKey(mapName)) {
            return new ArrayList<>(barrierCache.get(mapName));
        }

        if (loadBarriersFromJson(mapName)) {
            return new ArrayList<>(barrierCache.get(mapName));
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
    
    public MapInfo getMapInfo(String mapName) {
        List<Vector3> spawns = getSpawns(mapName);
        if (spawns.isEmpty()) return null;
        
        File file = new File(mapsFolder, mapName + ".json");
        
        return new MapInfo(
            mapName,
            spawns.size(),
            file.exists() ? file.lastModified() : 0
        );
    }
    
    private void saveToJson(String mapName, List<Vector3> spawns) {
        File file = new File(mapsFolder, mapName + ".json");
        
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, Object> data = new HashMap<>();
            data.put("map_name", mapName);
            data.put("spawn_count", spawns.size());
            data.put("valid_spawns", spawns);
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to save map: " + mapName, e);
        }
    }
    
    private void saveBarriersToJson(String mapName, List<Vector3> barriers) {
        File file = new File(barriersFolder, mapName + ".json");
        
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, Object> data = new HashMap<>();
            data.put("map_name", mapName);
            data.put("barrier_count", barriers.size());
            data.put("barriers", barriers);
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to save barriers: " + mapName, e);
        }
    }
    
    private boolean loadFromJson(String mapName) {
        File file = new File(mapsFolder, mapName + ".json");
        
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
            
            mapCache.put(mapName, spawns);
            return true;
            
        } catch (IOException e) {
            plugin.getLogger().error("Failed to load map: " + mapName, e);
            return false;
        }
    }
    
    private boolean loadBarriersFromJson(String mapName) {
        File file = new File(barriersFolder, mapName + ".json");
        
        if (!file.exists()) return false;
        
        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> data = gson.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
            
            List<Map<String, Double>> rawBarriers = (List<Map<String, Double>>) data.get("barriers");
            List<Vector3> barriers = new ArrayList<>();
            
            for (Map<String, Double> coords : rawBarriers) {
                barriers.add(new Vector3(
                    coords.get("x"),
                    coords.get("y"),
                    coords.get("z")
                ));
            }
            
            barrierCache.put(mapName, barriers);
            return true;
            
        } catch (IOException e) {
            plugin.getLogger().error("Failed to load barriers: " + mapName, e);
            return false;
        }
    }
    
    public boolean reloadMap(String mapName) {
        mapCache.remove(mapName);
        return loadFromJson(mapName);
    }
    
    public boolean reloadBarriers(String mapName) {
        barrierCache.remove(mapName);
        return loadBarriersFromJson(mapName);
    }
    
    public static class MapInfo {
        public final String name;
        public final int spawnCount;
        public final long lastModified;
        
        public MapInfo(String name, int spawnCount, long lastModified) {
            this.name = name;
            this.spawnCount = spawnCount;
            this.lastModified = lastModified;
        }
    }
}