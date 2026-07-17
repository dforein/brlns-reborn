package org.brlnsreb.core.minigame.match.game.arena;

import org.powernukkitx.level.Level;
import org.powernukkitx.level.Position;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public abstract class Arena {
    
    protected final String mapId;
    protected final String configPath;
    protected final String name;
    protected final Level level;
    protected final TimeOfDay time;
    protected final Weather weather;
    protected final Vector3 min;
    protected final Vector3 max;
    
    public Arena(Config config, String mapId, String mapsConfigPath, TimeOfDay time, Weather weather) {
        mapsConfigPath = YamlUtil.checkConfigPath(mapsConfigPath);

        this.mapId = mapId;
        this.configPath = mapsConfigPath;
        this.name = YamlUtil.getStr(configPath + "name", config);

        this.level = WorldManager.loadLevel(
            YamlUtil.getStr(configPath + "world", config), 
            config, 
            configPath
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
    public abstract Position getRandomSpawn();
    
    public boolean isInArena(Vector3 pos) {
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
