package org.brlnsreb.core.maps;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

public abstract class GameMapLevel extends MapLevel {

    public final Vector3 min;
    public final Vector3 max;
    public final String mapId;
    public final String name;

    public GameMapLevel(Config config, String configPath, String mapId, TimeOfDay time, Weather weather) {
        super(config, configPath, time, weather, true);

        this.mapId = mapId;
        this.name = YamlUtil.getStr(configPath + "name", config);
        this.min = YamlUtil.parseVector3(YamlUtil.getStr(configPath + "min", config));
        this.max = YamlUtil.parseVector3(YamlUtil.getStr(configPath + "max", config));
    }

    public boolean isInMap(Vector3 pos) {
        return pos.x >= min.x && pos.x <= max.x &&
               pos.y >= min.y && pos.y <= max.y &&
               pos.z >= min.z && pos.z <= max.z;
    }

    public boolean isNightVisionEnabled() {
        return config.getBoolean(configPath + "night-vision");
    }

    protected Level loadLevel(boolean copyworld) {
        return WorldManager.loadLevel(
            YamlUtil.getStr(configPath + "world", config), 
            config
        );
    }

    public boolean arePhysicsEnabled() {
        return config.getBoolean("settings.physics-enabled");
    }
    
}
