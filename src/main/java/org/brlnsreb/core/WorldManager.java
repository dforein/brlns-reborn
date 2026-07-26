package org.brlnsreb.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.brlnsreb.core.player.CustomPlayer;

import org.powernukkitx.Server;
import org.powernukkitx.level.GameRule;
import org.powernukkitx.level.GameRules;
import org.powernukkitx.level.Level;
import org.powernukkitx.utils.Config;

public class WorldManager {

    private static Server server;
    private static HashSet<Integer> enabledPhysicsLevels = new HashSet<>();
    private static final java.util.Set<String> reservedFolderNames = ConcurrentHashMap.newKeySet();

    public static void init() {
        server = Server.getInstance();
    }

    public static Level loadLobbyLevel(String levelName, boolean copyWorld) {
        return loadLevel(levelName, true, copyWorld, null);
    }

    public static Level loadLevel(String levelName, Config config) {
        return loadLevel(levelName, false, true, config);
    }

    private static Level loadLevel(String levelName, boolean isLobby, boolean copyWorld, Config config) {
        HashSet<String> availableLevels = getAllLevelNames();
        if (!availableLevels.contains(levelName)) return null;

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
            Path levelFolder = Path.of(worldsPath + folderName);
            if (!Files.exists(levelFolder)) {
                if (!copyWorld(Path.of(worldsPath + levelName), levelFolder)) {
                    reservedFolderNames.remove(folderName);
                    return null;
                }
                availableLevels.add(folderName);
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
        }, 20);

        CustomPlayer.removeLevel(level.getId());
        enabledPhysicsLevels.remove(level.getId());
    }

    public static void setGameRules(Level level) {
        setGameRules(level, true, null);
    }

    public static void setGameRules(Level level, Config config) {
        setGameRules(level, false, config);
    }

    private static void setGameRules(Level level, boolean isLobby, Config config) {
        GameRules gameRules = level.getGameRules();

        //particular
        GameRule[] particulars = {
            GameRule.PVP,
            GameRule.DO_FIRE_TICK,
            GameRule.DO_TILE_DROPS,
            GameRule.DO_ENTITY_DROPS,
            GameRule.DO_MOB_LOOT,
            GameRule.TNT_EXPLODES,
            GameRule.MOB_GRIEFING
        };

        for (GameRule rule : particulars) {
            gameRules.setGameRule(rule, 
                isLobby? false : config.getBoolean("settings.gamerules." + rule.getName(), false)  //default: false
            );
        }

        //universal (gets updated every time a new game needs something particular)
        GameRule[] enabled = {
            GameRule.DO_LIMITED_CRAFTING,
            GameRule.COMMAND_BLOCKS_ENABLED,
            GameRule.SEND_COMMAND_FEEDBACK,
            GameRule.DO_IMMEDIATE_RESPAWN,
            GameRule.COMMAND_BLOCK_OUTPUT
        };

        GameRule[] disabled = {
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

        for (GameRule rule : enabled) { gameRules.setGameRule(rule, true); }
        for (GameRule rule : disabled) { gameRules.setGameRule(rule, false); }

        level.save();
    }

    public static void enablePhysicsIn(Level level) {
        enabledPhysicsLevels.add(level.getId());
    }

    public static void enablePhysicsIn(int levelId) {
        enabledPhysicsLevels.add(levelId);
    }

    public static HashSet<Integer> getEnabledPhysicsLevels() { return enabledPhysicsLevels; }

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

    private static boolean copyWorld(Path source, Path destination) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = destination.resolve(source.relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, destination.resolve(source.relativize(file)), 
                            StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
            
        } catch (IOException e) {
            server.getLogger().error("Failed to copy world from " + source + " to " + destination, e);
            return false;
        }
    }

}
