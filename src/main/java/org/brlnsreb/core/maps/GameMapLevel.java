package org.brlnsreb.core.maps;

import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;

public abstract class GameMapLevel extends MapLevel {

    public final String mapId;
    public final String displayName;
    public final Vector3 min;
    public final Vector3 max;
    
    private boolean nightVision;

    public GameMapLevel(Minigame minigame, String mapId, TimeOfDay time, Weather weather) {
        super(
            minigame.getConfig(), 
            minigame.getMapSettings(), 
            "maps." + mapId + ".",
            time, weather, 
            true
        );

        this.mapId = mapId;
        this.displayName = YamlUtil.getStr(this.configPath + "name", this.mapSettings);

        this.min = YamlUtil.parseVector3(YamlUtil.getStr(this.configPath + "min", this.mapSettings));
        this.max = YamlUtil.parseVector3(YamlUtil.getStr(this.configPath + "max", this.mapSettings));

        this.nightVision = this.mapSettings.getBoolean(this.configPath + "night-vision");
    }

    public boolean isInMap(Vector3 pos) {
        return pos.x >= min.x && pos.x <= max.x &&
               pos.y >= min.y && pos.y <= max.y &&
               pos.z >= min.z && pos.z <= max.z;
    }

    public boolean isNightVisionEnabled() { 
        return nightVision;
    }

    protected Level loadLevel(boolean copyworld) {
        Level level = LevelManager.loadLevel(YamlUtil.getStr(configPath + "world", mapSettings), config);

        if (level == null) {
            configPath = "maps." + YamlUtil.getStr("default-map", mapSettings) + ".";
            level = LevelManager.loadLevel(YamlUtil.getStr(configPath + "world", mapSettings), config);
        }

        return level;
    }

    public boolean arePhysicsEnabled() {
        return config.getBoolean("settings.physics-enabled");
    }
    
}
