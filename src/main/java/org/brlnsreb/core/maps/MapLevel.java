package org.brlnsreb.core.maps;

import org.powernukkitx.level.Level;
import org.powernukkitx.level.Location;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public abstract class MapLevel {
    
    protected final String mapId;
    protected final String configPath;
    protected final String name;
    protected final Level level;
    protected final TimeOfDay time;
    protected final Weather weather;
    protected final Vector3 min;
    protected final Vector3 max;
    
    public MapLevel(Config config, String configPath, String mapId, TimeOfDay time, Weather weather) {
        configPath = YamlUtil.checkConfigPath(configPath);

        this.mapId = mapId;
        this.configPath = configPath;
        this.name = YamlUtil.getStr(configPath, config);

        this.level = WorldManager.loadLevel(
            YamlUtil.getStr(configPath + "world", config), 
            config
        );

        this.time = TimeOfDay.setTime(level, time);
        this.weather = Weather.setWeather(level, weather);

        this.min = YamlUtil.parseVector3(YamlUtil.getStr(configPath + "min", config));
        this.max = YamlUtil.parseVector3(YamlUtil.getStr(configPath + "max", config));

        loadSpawns(config);

        if (config.getBoolean("settings.physics-enabled")) {
            WorldManager.enablePhysicsIn(level);
        }
    }

    public void close() {
        WorldManager.unloadLevel(level);
    }
    
    protected abstract void loadSpawns(Config config);
    public abstract Location getRandomSpawn(CustomPlayer player);
    
    public boolean isInMap(Vector3 pos) {
        return pos.x >= min.x && pos.x <= max.x &&
               pos.y >= min.y && pos.y <= max.y &&
               pos.z >= min.z && pos.z <= max.z;
    }

    public String getMapId() { return mapId; }
    public String getConfigPath() { return configPath; }
    public String getName() { return name; }
    public Level getLevel() { return level; }
    public TimeOfDay getTime() { return time; }
    public Weather getWeather() { return weather; }
    public Vector3 getMin() { return min; }
    public Vector3 getMax() { return max; }

}
