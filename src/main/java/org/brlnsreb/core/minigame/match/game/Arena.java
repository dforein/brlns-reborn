package org.brlnsreb.core.minigame.match.game;

import org.powernukkitx.level.Level;
import org.powernukkitx.level.Position;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

import java.util.List;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

import java.util.ArrayList;
import java.util.Collections;

public class Arena {
    
    private final String configPath;
    private final String name;
    private final Level level;
    private final Vector3 min;
    private final Vector3 max;

    private final List<Position> spawns;
    private int spawnIndex = 0;
    
    public Arena(Config config, String mapsConfigPath, String settingsConfigPath, TimeOfDay time, Weather weather) {
        mapsConfigPath = YamlUtil.checkConfigPath(mapsConfigPath);
        settingsConfigPath = YamlUtil.checkConfigPath(settingsConfigPath);

        this.configPath = mapsConfigPath;
        this.name = YamlUtil.getStr(configPath + "name", config);

        this.level = WorldManager.loadLevel(
            YamlUtil.getStr(configPath + "world", config), 
            config, 
            configPath
        );

        TimeOfDay.setTime(level, time);
        Weather.setWeather(level, weather);

        this.min = YamlUtil.parseVector3(YamlUtil.getStr(configPath + "min", config));
        this.max = YamlUtil.parseVector3(YamlUtil.getStr(configPath + "max", config));

        this.spawns = new ArrayList<>();
        for (String rawCoords : config.getStringList(configPath + "spawns")) {
            this.spawns.add(Position.fromObject(
                YamlUtil.parseVector3Centered(rawCoords), 
                level
            ));
        }
        Collections.shuffle(spawns);

        if (config.getBoolean(settingsConfigPath + "physics-enabled")) {
            WorldManager.enablePhysicsIn(level);
        }
    }

    public void close() {
        WorldManager.unloadLevel(level);
    }
    
    public Position getRandomSpawn() {
        if (spawns.isEmpty()) return null;
        if (spawnIndex >= spawns.size()) spawnIndex = 0;
        return spawns.get(spawnIndex++);
    }
    
    public boolean isInArena(Vector3 pos) {
        return pos.x >= min.x && pos.x <= max.x &&
               pos.y >= min.y && pos.y <= max.y &&
               pos.z >= min.z && pos.z <= max.z;
    }

    public String getConfigPath() { return configPath; }
    public String getName() { return name; }
    public Level getLevel() { return level; }
    public Vector3 getMin() { return min; }
    public Vector3 getMax() { return max; }
    public List<Position> getSpawns() { return spawns; }

}