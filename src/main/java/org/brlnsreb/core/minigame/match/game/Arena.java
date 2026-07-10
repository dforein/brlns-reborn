package org.brlnsreb.core.minigame.match.game;

import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

import java.util.List;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

import java.util.ArrayList;

public class Arena {
    
    private final String name;
    private final Level level;
    private final Vector3 min;
    private final Vector3 max;
    private final List<Vector3> spawns;
    
    public Arena(Config config, String mapsConfigPath, String settingsConfigPath, TimeOfDay time, Weather weather) {
        mapsConfigPath = YamlUtil.checkConfigPath(mapsConfigPath);
        settingsConfigPath = YamlUtil.checkConfigPath(settingsConfigPath);

        this.name = YamlUtil.getStr(mapsConfigPath + "name", config);

        this.level = WorldManager.loadLevel(
            YamlUtil.getStr(mapsConfigPath + "world", config), 
            config, 
            mapsConfigPath
        );

        TimeOfDay.setTime(level, time);
        Weather.setWeather(level, weather);

        this.min = YamlUtil.parseVector3(YamlUtil.getStr(mapsConfigPath + "min", config));
        this.max = YamlUtil.parseVector3(YamlUtil.getStr(mapsConfigPath + "max", config));

        this.spawns = new ArrayList<>();
        for (String rawCoords : config.getStringList(mapsConfigPath + "spawns")) {
            this.spawns.add(YamlUtil.parseVector3Centered(rawCoords));
        }

        if (config.getBoolean(settingsConfigPath + "physics-enabled")) {
            WorldManager.enablePhysicsIn(level);
        }
    }

    public void close() {
        WorldManager.unloadLevel(level);
    }
    
    public String getName() {
        return name;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public Vector3 getMin() {
        return min;
    }
    
    public Vector3 getMax() {
        return max;
    }
    
    public List<Vector3> getSpawns() {
        return spawns;
    }
    
    public Vector3 getRandomSpawn() {
        if (spawns.isEmpty()) return new Vector3(0, 64, 0);
        return spawns.get((int)(Math.random() * spawns.size()));
    }
    
    public boolean isInArena(Vector3 pos) {
        return pos.x >= min.x && pos.x <= max.x &&
               pos.y >= min.y && pos.y <= max.y &&
               pos.z >= min.z && pos.z <= max.z;
    }
}