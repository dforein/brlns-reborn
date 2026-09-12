package org.brlnsreb.minigames.mm.match.game.systems;

import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.AxisAlignedBB;
import org.powernukkitx.math.BlockFace;
import org.powernukkitx.math.Vector3;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.utils.Vect;
import org.brlnsreb.utils.messages.ChatMsgs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class GoldSpawnMapper {

    private static final Map<String, List<Vector3>> mapCache = new HashMap<>();
    private static final File mapsFolder = new File(BrlnsReb.instance.getDataFolder(), "mm/maps");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, Set<Vector3>> mapIncludeWIP = new HashMap<>();
    private static final Map<String, Set<Vector3>> mapExcludeWIP = new HashMap<>();
    public static enum Operation {
        INCLUDE(true, true), 
        EXCLUDE(false, true), 
        INCLUDE_IGNORE(true, false), 
        EXCLUDE_IGNORE(false, false);

        public final boolean value;
        public final boolean overwrite;

        private Operation(boolean value, boolean overwrite) {
            this.value = value;
            this.overwrite = overwrite;
        }
    }

    static {
        if (!mapsFolder.exists()) mapsFolder.mkdirs();
    }
    
    private static final double MIN_SPACE = 1.8;
    private static final HashSet<String> SAFE_PASSABLE_BLOCKS = new HashSet<>(List.of(
        Block.VINE, Block.CAVE_VINES, Block.WEEPING_VINES, Block.TWISTING_VINES,
        Block.LADDER, Block.SCAFFOLDING
    ));
    private static final HashSet<String> UNSAFE_PASSABLE_BLOCKS = new HashSet<>(List.of(
        Block.LAVA, Block.FLOWING_LAVA,
        Block.WEB, Block.SWEET_BERRY_BUSH
    ));
    
    private static Vector3 getValidPos(Level level, Vect pos) {
        //this should be a one to one function (int Vect to valid Vector3)

        Block target = pos.getBlock(level);
        if (UNSAFE_PASSABLE_BLOCKS.contains(target.getId())) return null;
        Block below = pos.add(-1.0, Vect.Y).getBlock(level);
        Block above = pos.add(2.0, Vect.Y).getBlock(level);
        if (above.isSolid(BlockFace.DOWN)) return null;         //it means there is less than a block of height
        if (UNSAFE_PASSABLE_BLOCKS.contains(above.getId())) return null;
        Block above2 = pos.add(1.0, Vect.Y).getBlock(level);

        AxisAlignedBB targetBB = target.getBoundingBox();
        AxisAlignedBB belowBB = below.getBoundingBox();
        AxisAlignedBB aboveBB = above.getBoundingBox();
        AxisAlignedBB above2BB = above2.getBoundingBox();

        boolean isBaseBelow = true;

        //BASE BLOCK CHECKS + DEFINE CANDIDATE POS

        //is target passable?
        if (isSafePassable(target)) {
            //then is below solid? (only the upper face)
            if (!below.isSolid(BlockFace.UP)) return null;
        } else {
            //else:
            //then is target solid down?
            if (!target.isSolid(BlockFace.DOWN)) return null;
            //then is target not solid up? (if it's solid, it's invalid by default)
            if (target.isSolid(BlockFace.UP)) return null;
            isBaseBelow = false;
        }
        
        Vector3 candidate = pos.set(
            isBaseBelow ? belowBB.getMaxY() : targetBB.getMaxY(), 
            Vect.Y
        ).getNewVector3();

        //MINIMUM SPACE CHECK
        
        double measuredHeight = 0.0;

        //measure target height (below is ignored since it's always solid on top)
        if (isBaseBelow) {
            measuredHeight++;       //target is a whole passable block
        } else {
            measuredHeight += target.getFloorY() + 1.0 - targetBB.getMaxY();
        }

        //measure above height
        if (isSafePassable(above) || aboveBB == null) {
            measuredHeight++;
        } else {
            measuredHeight += above.getFloorY() + 1.0 - aboveBB.getMaxY();
        }
        if (measuredHeight >= MIN_SPACE) return candidate;

        //measure above2 height
        if (isSafePassable(above2) || above2BB == null) {
            measuredHeight++;
        } else {
            measuredHeight += above2.getFloorY() + 1.0 - above2BB.getMaxY();
        }
        if (measuredHeight >= MIN_SPACE) return candidate;

        return null;
    }

    private static boolean isSafePassable(Block block) {
        return !UNSAFE_PASSABLE_BLOCKS.contains(block.getId())
            && ( block.canPassThrough() || SAFE_PASSABLE_BLOCKS.contains(block.getId()) );
    }

    public static int startMapping(String mapId) {
        List<Vector3> spawns = mapCache.get(mapId);

        if (spawns == null) {
            if (!loadFromJson(mapId)) {
                spawns = new ArrayList<>();
                mapCache.put(mapId, spawns);
            } else {
                spawns = mapCache.get(mapId);
            }
        }

        if (!mapIncludeWIP.containsKey(mapId)) {
            mapIncludeWIP.put(mapId, new HashSet<>(spawns));
            mapExcludeWIP.put(mapId, new HashSet<>());
        }

        return spawns.size();
    }

    public static void finishMapping(String mapId) {
        mapCache.put(mapId, new ArrayList<>(mapIncludeWIP.get(mapId)));

        saveToJson(mapId);
        
        mapIncludeWIP.remove(mapId);
        mapExcludeWIP.remove(mapId);
    }

    public static void leaveMapping(String mapId) {
        mapIncludeWIP.remove(mapId);
        mapExcludeWIP.remove(mapId);
    }

    public static void volumeOperation(Operation op, Player player, String mapId, Vector3 pos1, Vector3 pos2) {
        Set<Vector3> mainSet = op.value ? mapIncludeWIP.get(mapId) : mapExcludeWIP.get(mapId);
        Set<Vector3> dualSet = op.value ? mapExcludeWIP.get(mapId) : mapIncludeWIP.get(mapId);

        double minX = Math.floor(Math.min(pos1.x, pos2.x));
        double minY = Math.floor(Math.min(pos1.y, pos2.y));
        double minZ = Math.floor(Math.min(pos1.z, pos2.z));

        double maxX = Math.floor(Math.max(pos1.x, pos2.x));
        double maxY = Math.floor(Math.max(pos1.y, pos2.y));
        double maxZ = Math.floor(Math.max(pos1.z, pos2.z));

        Level level = player.level;
        Vect curr = new Vect();

        int scanned = (int) ((maxX - minX + 1.0) * (maxY - minY + 1.0) * (maxZ - minZ + 1.0));
        int moved = 0, alreadyMoved = 0, ignored = 0;

        player.sendMessage("§eScanning volume...");

        for (double x = minX; x <= maxX; x++) {
        for (double y = minY; y <= maxY; y++) {
        for (double z = minZ; z <= maxZ; z++) {

            Vector3 valid = getValidPos(level, curr.set(x, y, z));
            if (valid == null) continue;

            if (mainSet.contains(valid)) {
                alreadyMoved++;
                continue;
            }

            if (op.overwrite) {
                mainSet.add(valid);
                dualSet.remove(valid);
                moved++;
            } else {
                if (!dualSet.contains(valid)) {
                    mainSet.add(valid);
                    moved++;
                } else ignored++;
            }

        }}}
        
        player.sendMessage(ChatMsgs.BAR);
        player.sendMessage("§dScanned " + scanned + " blocks");
        player.sendMessage("- §a"+ (op.value ? "Included" : "Excluded") +" "+moved+" spawns from volume");
        if (alreadyMoved > 0) {
            player.sendMessage("- §eIgnored " + alreadyMoved + " already "+ (op.value ? "included" : "excluded") +" spawns from volume");
        }
        if (ignored > 0) {
            player.sendMessage("- §eIgnored " + ignored + " "+ (op.value ? "excluded" : "included") +" spawns from volume");
        }
        player.sendMessage("§2NEW TOTAL: §l" + mapIncludeWIP.get(mapId).size() + " spawns");
        player.sendMessage(ChatMsgs.BAR);
    }
    
    public static List<Vector3> getSpawns(String mapId) {
        if (mapCache.containsKey(mapId)) {
            return new ArrayList<>(mapCache.get(mapId));
        }

        if (loadFromJson(mapId)) {
            return new ArrayList<>(mapCache.get(mapId));
        }
        
        return new ArrayList<>();
    }
    
    private static void saveToJson(String mapId) {
        List<Vector3> spawns = mapCache.get(mapId);
        if (spawns == null || spawns.isEmpty()) return;

        try (FileWriter writer = new FileWriter(new File(mapsFolder, mapId + ".json"))) {
            Map<String, Object> data = new HashMap<>();
            data.put("map_id", mapId);
            data.put("spawn_count", spawns.size());
            data.put("valid_spawns", spawns);
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            BrlnsReb.logger.error("Failed to save map: " + mapId, e);
        }
    }
    
    private static boolean loadFromJson(String mapId) {
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
    
    public static boolean reloadMap(String mapId) {
        mapCache.remove(mapId);
        return loadFromJson(mapId);
    }

}