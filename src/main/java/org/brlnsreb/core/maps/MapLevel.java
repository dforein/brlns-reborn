package org.brlnsreb.core.maps;

import org.powernukkitx.Player;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Location;
import org.powernukkitx.utils.Config;

import java.util.Map;

import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;

public abstract class MapLevel {
    
    protected final Config config;
    protected final Config mapSettings;
    public final String configPath;

    public final Level level;
    public final TimeOfDay time;
    public final Weather weather;

    public MapLevel(Config config, String configPath, boolean copyWorld) {
        this(config, null, configPath, null, null, copyWorld);
    }
    
    public MapLevel(Config config, Config mapSettings, String configPath, TimeOfDay time, Weather weather, boolean copyWorld) {
        this.config = config;
        this.mapSettings = mapSettings;
        this.configPath = YamlUtil.checkConfigPath(configPath);

        this.level = loadLevel(copyWorld);
        this.time = TimeOfDay.setTime(level, time);
        this.weather = Weather.setWeather(level, weather);

        loadSpawns();

        if (arePhysicsEnabled()) {
            LevelManager.enablePhysicsIn(level);
        }
    }

    public void close() {
        LevelManager.unloadLevel(level);
    }
    
    protected abstract Level loadLevel(boolean copyWorld);
    protected abstract void loadSpawns();
    protected abstract boolean arePhysicsEnabled();
    public abstract Location getSpawnFor(CustomPlayer player);
    public Map<Long, Player> getPlayers() { return level.getPlayers(); }

}
