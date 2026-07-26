package org.brlnsreb.core.maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.level.Location;
import org.powernukkitx.level.Position;
import org.powernukkitx.utils.Config;

public class RandomSpawnsMap extends MapLevel {

    private List<Position> spawns;
    private int spawnIndex = 0;
    
    public RandomSpawnsMap(Config config, String mapId, String mapsConfigPath, TimeOfDay time, Weather weather) {
        super(config, mapId, mapsConfigPath, time, weather);
    }

    protected void loadSpawns(Config config) {
        if (spawns == null) spawns = new ArrayList<>();

        for (String rawCoords : config.getStringList(configPath + "spawns")) {
            this.spawns.add(Position.fromObject(
                YamlUtil.parseVector3Centered(rawCoords), 
                level
            ));
        }
        Collections.shuffle(spawns);
    }

    public Location getRandomSpawn() {
        if (spawns.isEmpty()) return null;
        if (spawnIndex >= spawns.size()) spawnIndex = 0;
        return spawns.get(spawnIndex++).getLocation();
    }

    public List<Position> getSpawns() { return spawns; }

}