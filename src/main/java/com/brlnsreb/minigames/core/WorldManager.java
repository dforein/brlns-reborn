package com.brlnsreb.minigames.core;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.brlnsreb.minigames.utils.YamlUtil;

import cn.nukkit.Server;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

public class WorldManager {

    private static Server server;

    public static void init() {
        server = Server.getInstance();
    }

    public static Level loadLobbyLevel(String levelName) {
        return loadLevel(levelName, true, null, null);
    }

    public static Level loadLevel(String levelName, Config config, String configPath) {
        return loadLevel(levelName, false, config, configPath);
    }

    private static Level loadLevel(String levelName, boolean isLobby, Config config, String configPath) {
        configPath = YamlUtil.checkConfigPath(configPath);

        HashSet<String> availableLevels = getAllLevelNames();
        if (!availableLevels.contains(levelName)) return null;

        String folderName = levelName;
        if (!isLobby) {
            //get lowest X number in "levelNameX" available for the level to load
            int count = 1;
            while (server.getLevelByName(folderName) != null) {
                count++;
                folderName = levelName + count;
            }

            //check if folder of levelNameX exists, else create it
            String worldPath = server.getDataPath() + "/worlds/";
            Path levelFolder = Path.of(worldPath + folderName);
            if (!Files.exists(levelFolder)) {
                if (!copyWorld(Path.of(worldPath + levelName), levelFolder)) return null;
                availableLevels.add(folderName);
            }
        }
        
        //load level
        server.loadLevel(folderName);
        Level loadedLevel = server.getLevelByName(folderName);
        loadedLevel.setAutoSave(false);

        setGameRules(loadedLevel, isLobby, config, configPath);

        return loadedLevel;
    }

    public static void unloadLevel(Level level) {
        server.getScheduler().scheduleDelayedTask(() -> { server.unloadLevel(level, true); }, 20);
    }

    public static void setGameRules(Level level) {
        setGameRules(level, true, null, null);
    }

    public static void setGameRules(Level level, Config config, String configPath) {
        setGameRules(level, false, config, configPath);
    }

    private static void setGameRules(Level level, boolean isLobby, Config config, String configPath) {
        //TODO: interaction with world (e.g. skywars: drops, tnts, fire spread, etc allowed)
        
        configPath = YamlUtil.checkConfigPath(configPath);

        GameRules gameRules = level.getGameRules();

        //particular
        GameRule[] particulars = {
            GameRule.NATURAL_REGENERATION,
            GameRule.PVP
        };

        for (GameRule rule : particulars) {
            gameRules.setGameRule(rule, 
                isLobby? false : config.getBoolean(configPath + "gamerules." + rule.getName())
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
            GameRule.DO_DAYLIGHT_CYCLE,
            GameRule.DO_ENTITY_DROPS,
            GameRule.DO_FIRE_TICK,
            GameRule.DO_INSOMNIA,
            GameRule.DO_MOB_LOOT,
            GameRule.DO_MOB_SPAWNING,
            GameRule.DO_TILE_DROPS,
            GameRule.DO_WEATHER_CYCLE,
            GameRule.SHOW_DAYS_PLAYED,
            GameRule.RECIPES_UNLOCK,
            GameRule.SHOW_COORDINATES,
            GameRule.TNT_EXPLODES,
            GameRule.PROJECTILES_CAN_BREAK_BLOCKS,
            GameRule.MOB_GRIEFING,
            GameRule.LOCATOR_BAR
        };

        for (GameRule rule : enabled) { gameRules.setGameRule(rule, true); }
        for (GameRule rule : disabled) { gameRules.setGameRule(rule, false); }

        level.save();
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
