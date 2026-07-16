package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockBarrier;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Position;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.TextFormat;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.game.arena.RandomSpawnsArena;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class GoldSpawnMapper {
    
    private final BrlnsReb plugin;
    private final Map<String, List<Vector3>> mapCache;
    private final Map<String, List<Vector3>> barrierCache;
    private final File mapsFolder;
    private final File barriersFolder;
    private final Gson gson;
    
    private static final HashSet<String> SAFE_PASSABLE = new HashSet<>(Arrays.asList(
            Block.AIR,
            Block.TALL_GRASS, Block.TALL_DRY_GRASS,
            Block.SHORT_GRASS, Block.SHORT_DRY_GRASS,
            Block.RED_CARPET, Block.CYAN_CARPET,
            Block.BLUE_CARPET, Block.GRAY_CARPET, Block.LIME_CARPET,Block.MOSS_CARPET, 
            Block.PINK_CARPET, Block.BLACK_CARPET, Block.BROWN_CARPET, Block.GREEN_CARPET,
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
        )
    );
    
    public GoldSpawnMapper() {
        this.plugin = BrlnsReb.instance;
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
    
    public void scanArena(RandomSpawnsArena arena, String mapId, Player admin) {
        scanArena(arena, mapId, admin, false);
    }

    public void scanArena(RandomSpawnsArena arena, String mapId, Player admin, boolean useBarrierWhitelist) {
        admin.sendMessage(TextFormat.YELLOW + "Starting scan for map: " + mapId);
        if (useBarrierWhitelist) {
            admin.sendMessage(TextFormat.GRAY + "Using barrier whitelist mode");
        }
        admin.sendMessage(TextFormat.GRAY + "This may take a while...");
        
        Set<Vector3> whitelistedBarriers = new HashSet<>();
        if (useBarrierWhitelist) {
            List<Vector3> barriers = getBarriers(mapId);
            if (barriers.isEmpty()) {
                admin.sendMessage(TextFormat.RED + "No barrier file found for " + mapId);
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
        
        saveToJson(mapId, validSpawns);
        mapCache.put(mapId, validSpawns);
        
        admin.sendMessage(TextFormat.GREEN + "Scan completed!");
        admin.sendMessage(TextFormat.GOLD + "Found: " + validSpawns.size() + " valid spawns");
        admin.sendMessage(TextFormat.GRAY + "Time: " + (elapsed / 1000.0) + "s");
        admin.sendMessage(TextFormat.GRAY + "Saved to: maps/" + mapId + ".json");
    }
    
    public void scanForBarriers(RandomSpawnsArena arena, String mapId, Player admin) {
        admin.sendMessage(TextFormat.YELLOW + "Starting barrier scan for map: " + mapId);
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
                    
                    if (block instanceof BlockBarrier) {
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
        
        saveBarriersToJson(mapId, barriers);
        barrierCache.put(mapId, barriers);
        
        admin.sendMessage(TextFormat.GREEN + "Barrier scan completed!");
        admin.sendMessage(TextFormat.GOLD + "Found: " + barriers.size() + " barriers");
        admin.sendMessage(TextFormat.GRAY + "Time: " + (elapsed / 1000.0) + "s");
        admin.sendMessage(TextFormat.GRAY + "Saved to: barriers/" + mapId + ".json");
    }
    
    public void countBarriers(RandomSpawnsArena arena, Player admin) {
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
                    
                    if (block instanceof BlockBarrier) {
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
        if (isBelowWhitelisted && blockBelowId.equals(Block.BARRIER)) {
            isBelowValid = true;
        } else {
            isBelowValid = blockBelow.isSolid() && !blockBelowId.equals(Block.BARRIER);
        }
        
        // Conditions: passable block, valid below, passable above
        return SAFE_PASSABLE.contains(blockTargetId) &&
            isBelowValid &&
            SAFE_PASSABLE.contains(blockAboveId);
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
    
    public List<Vector3> getBarriers(String mapId) {
        if (barrierCache.containsKey(mapId)) {
            return new ArrayList<>(barrierCache.get(mapId));
        }

        if (loadBarriersFromJson(mapId)) {
            return new ArrayList<>(barrierCache.get(mapId));
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
    
    public MapInfo getMapInfo(String mapId) {
        List<Vector3> spawns = getSpawns(mapId);
        if (spawns.isEmpty()) return null;
        
        File file = new File(mapsFolder, mapId + ".json");
        
        return new MapInfo(
            mapId,
            spawns.size(),
            file.exists() ? file.lastModified() : 0
        );
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
            plugin.getLogger().error("Failed to save map: " + mapId, e);
        }
    }
    
    private void saveBarriersToJson(String mapId, List<Vector3> barriers) {
        File file = new File(barriersFolder, mapId + ".json");
        
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, Object> data = new HashMap<>();
            data.put("map_name", mapId);
            data.put("barrier_count", barriers.size());
            data.put("barriers", barriers);
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to save barriers: " + mapId, e);
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
            plugin.getLogger().error("Failed to load map: " + mapId, e);
            return false;
        }
    }
    
    private boolean loadBarriersFromJson(String mapId) {
        File file = new File(barriersFolder, mapId + ".json");
        
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
            
            barrierCache.put(mapId, barriers);
            return true;
            
        } catch (IOException e) {
            plugin.getLogger().error("Failed to load barriers: " + mapId, e);
            return false;
        }
    }
    
    public boolean reloadMap(String mapId) {
        mapCache.remove(mapId);
        return loadFromJson(mapId);
    }
    
    public boolean reloadBarriers(String mapId) {
        barrierCache.remove(mapId);
        return loadBarriersFromJson(mapId);
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