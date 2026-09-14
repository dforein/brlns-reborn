package org.brlnsreb.core.levels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.powernukkitx.Server;
import org.powernukkitx.level.GameRule;
import org.powernukkitx.level.GameRules;
import org.powernukkitx.level.Level;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class LevelManager {

    private static final GameRule[] PARTICULAR_GAME_RULES = {
        GameRule.PVP,
        GameRule.DO_FIRE_TICK,
        GameRule.DO_TILE_DROPS,
        GameRule.DO_ENTITY_DROPS,
        GameRule.DO_MOB_LOOT,
        GameRule.TNT_EXPLODES,
        GameRule.MOB_GRIEFING
    };

    private static final GameRule[] ENABLED_GAME_RULES = {
        GameRule.DO_LIMITED_CRAFTING,
        GameRule.COMMAND_BLOCKS_ENABLED,
        GameRule.SEND_COMMAND_FEEDBACK,
        GameRule.DO_IMMEDIATE_RESPAWN,
        GameRule.COMMAND_BLOCK_OUTPUT
    };

    private static final GameRule[] DISABLED_GAME_RULES = {
        GameRule.NATURAL_REGENERATION,
        GameRule.DO_DAYLIGHT_CYCLE,
        GameRule.DO_INSOMNIA,
        GameRule.DO_MOB_SPAWNING,
        GameRule.DO_WEATHER_CYCLE,
        GameRule.SHOW_DAYS_PLAYED,
        GameRule.RECIPES_UNLOCK,
        GameRule.SHOW_COORDINATES,
        GameRule.PROJECTILES_CAN_BREAK_BLOCKS,
        GameRule.LOCATOR_BAR
    };

    private static final String DUPLICATE_FOLDERS_TXT_PATH = BrlnsReb.instance.getDataFolder() + "/zz__internal_data/duplicate-folders.txt";

    private static Server server;
    private static final Set<Integer> mainLobbyLevels = new HashSet<>();
    private static final Map<Integer, Integer> enabledPhysicsLevels = new HashMap<>();        // level id -> range of blocks from players (-1 = all world)
    private static final Set<String> reservedFolderNames = ConcurrentHashMap.newKeySet();

    public static void init() {
        server = Server.getInstance();
    }

    //level loading

    public static void loadAllLevelsUnderMaintenance() {
        removeDuplicateWorldFolders();

        for (String levelName : getAllLevelNames()) {
            server.loadLevel(levelName);
            GameRules gameRules = server.getLevelByName(levelName).getGameRules();
            
            for (GameRule rule : PARTICULAR_GAME_RULES) {
                gameRules.setGameRule(rule, false);
            }
            for (GameRule rule : ENABLED_GAME_RULES) {
                gameRules.setGameRule(rule, true);
            }
            for (GameRule rule : DISABLED_GAME_RULES) {
                gameRules.setGameRule(rule, false);
            }
            gameRules.setGameRule(GameRule.SHOW_COORDINATES, true);
            gameRules.setGameRule(GameRule.LOCATOR_BAR, true);
        }

        Config mainHubConfig = Configs.getConfig("main_hub/config.yml");
        server.setDefaultLevel(
            server.getLevelByName(mainHubConfig.getString("world"))
        );
        server.getDefaultLevel().setSpawnLocation(
            YamlUtil.parseVector3(mainHubConfig.getString("spawn-pos"))
        );
    }

    public static Level loadLobbyLevel(String levelName, boolean copyWorld) {
        Level newLevel = loadLevel(levelName, true, copyWorld, null);
        if (!copyWorld) mainLobbyLevels.add(newLevel.getId());
        return newLevel;
    }

    public static Level loadLevel(String levelName, Config config) {
        return loadLevel(levelName, false, true, config);
    }

    private static Level loadLevel(String levelName, boolean isLobby, boolean copyWorld, Config config) {
        if (!getAllLevelNames().contains(levelName)) return null;

        String folderName = levelName;
        
        if (copyWorld) {
            //get lowest X number in "levelNameX" available for the level to load
            int count = 1;
            synchronized (reservedFolderNames) {
                while (reservedFolderNames.contains(folderName) || server.getLevelByName(folderName) != null) {
                    count++;
                    folderName = levelName + count;
                }
                reservedFolderNames.add(folderName);
            }

            //check if folder of levelNameX exists, else create it
            String worldsPath = server.getDataPath() + "/worlds/";
            if (!Files.exists(Path.of(worldsPath + folderName))) {
                try {
                    FileUtils.copyDirectory(new File(worldsPath + levelName), new File(worldsPath + folderName));
                } catch (IOException e) {
                    BrlnsReb.logger.error("Error during world duplication: " + e.getMessage());
                    reservedFolderNames.remove(folderName);
                    return null;
                }
                
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(DUPLICATE_FOLDERS_TXT_PATH, true))) {
                    bw.write(folderName);
                    bw.newLine();
                } catch (IOException e) {
                    BrlnsReb.logger.error("Writing error with duplicate-folders.txt: " + e.getMessage());
                }
            }
        }
        
        //load level
        server.loadLevel(folderName);
        Level loadedLevel = server.getLevelByName(folderName);
        loadedLevel.setAutoSave(false);

        setGameRules(loadedLevel, isLobby, config);

        return loadedLevel;
    }

    public static void unloadLevel(int levelId) {
        unloadLevel(server.getLevel(levelId));
    }

    public static void unloadLevel(Level level) {
        String folderName = level.getName();

        server.getScheduler().scheduleDelayedTask(() -> {
            server.unloadLevel(level, true);
            reservedFolderNames.remove(folderName);
        }, 5);

        CustomPlayer.removeLevel(level.getId());
        enabledPhysicsLevels.remove(level.getId());
    }


    //gamerules

    public static void setGameRules(Level level) {
        setGameRules(level, true, null);
    }

    public static void setGameRules(Level level, Config config) {
        setGameRules(level, false, config);
    }

    private static void setGameRules(Level level, boolean isLobby, Config config) {
        GameRules gameRules = level.getGameRules();

        //particular
        for (GameRule rule : PARTICULAR_GAME_RULES) {
            gameRules.setGameRule(rule, 
                isLobby? false : config.getBoolean("settings.gamerules." + rule.getName(), false)  //default: false
            );
        }

        //universal (gets updated every time a new game needs something particular)
        for (GameRule rule : ENABLED_GAME_RULES) { gameRules.setGameRule(rule, true); }
        for (GameRule rule : DISABLED_GAME_RULES) { gameRules.setGameRule(rule, false); }

        if (BrlnsReb.isUnderMaintenance()) {
            //don't save, and enable show coordinates
            gameRules.setGameRule(GameRule.SHOW_COORDINATES, true);
        } else {
            level.save();
        }
    }


    //physics

    public static void enablePhysicsIn(Level level, Integer range) {
        enabledPhysicsLevels.put(level.getId(), range);
    }

    public static void enablePhysicsIn(Integer levelId, Integer range) {
        enabledPhysicsLevels.put(levelId, range);
    }

    public static void enablePhysicsIn(String levelName, Integer range) {
        enabledPhysicsLevels.put(server.getLevelByName(levelName).getId(), range);
    }

    public static void disablePhysicsIn(Level level) {
        enabledPhysicsLevels.remove(level.getId());
    }

    public static void disablePhysicsIn(Integer levelId) {
        enabledPhysicsLevels.remove(levelId);
    }

    public static void disablePhysicsIn(String levelName) {
        enabledPhysicsLevels.remove(server.getLevelByName(levelName).getId());
    }

    public static boolean arePhysicsEnabledIn(Level level) {
        return enabledPhysicsLevels.containsKey(level.getId());
    }

    public static boolean arePhysicsEnabledIn(Integer levelId) {
        return enabledPhysicsLevels.containsKey(levelId);
    }

    public static boolean arePhysicsEnabledIn(String levelName) {
        return enabledPhysicsLevels.containsKey(server.getLevelByName(levelName).getId());
    }

    public static Integer getPhysicsRangeIn(Level level) {
        return enabledPhysicsLevels.get(level.getId());
    }

    public static Integer getPhysicsRangeIn(Integer levelId) {
        return enabledPhysicsLevels.get(levelId);
    }

    public static Integer getPhysicsRangeIn(String levelName) {
        return enabledPhysicsLevels.get(server.getLevelByName(levelName).getId());
    }

    public static Map<Integer, Integer> getEnabledPhysicsLevels() { return enabledPhysicsLevels; }


    //utils

    public static boolean isMainLobby(Level level) {
        return mainLobbyLevels.contains(level.getId());
    }

    public static HashSet<String> getAllLevelNames() {
        Path worldsFolder = Path.of(server.getDataPath() + "/worlds");
        if (!Files.exists(worldsFolder) || !Files.isDirectory(worldsFolder)) {
            return null;
        }

        try (Stream<Path> stream = Files.list(worldsFolder)) {
            return new HashSet<>(
                stream
                    .filter(Files::isDirectory)
                    .filter(dir -> Files.exists(dir.resolve("level.dat")))
                    .map(dir -> dir.getFileName().toString())
                    .collect(Collectors.toSet())
            );
            
        } catch (IOException e) {
            server.getLogger().error("Failed to get all level names", e);
            return null;
        }
    }

    public static void removeDuplicateWorldFolders() {
        Path path = Paths.get(DUPLICATE_FOLDERS_TXT_PATH);
        if (!Files.exists(path)) return;

        int removed = 0;
        try {
            String worldsPath = server.getDataPath() + "/worlds/";
            for (String duplicate : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                FileUtils.deleteDirectory(new File(worldsPath + duplicate));
                removed++;
            }

            Files.delete(path);

        } catch (IOException e) {
            BrlnsReb.logger.error("Error during duplicate folders deletion: " + e.getMessage());
        }

        int removedFolders = removed;
        BrlnsReb.getScheduler().scheduleTask(
            () -> BrlnsReb.logger.info(TextFormat.GREEN + "Removed " + removedFolders + " duplicate world folders.")
        );
    }

}
