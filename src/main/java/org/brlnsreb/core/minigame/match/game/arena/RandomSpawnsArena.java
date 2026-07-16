package org.brlnsreb.core.minigame.match.game.arena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.level.Position;
import org.powernukkitx.utils.Config;

public class RandomSpawnsArena extends Arena {

    private final List<Position> spawns = new ArrayList<>();
    private int spawnIndex = 0;
    
    public RandomSpawnsArena(Config config, String mapId, String mapsConfigPath, TimeOfDay time, Weather weather) {
        super(config, mapId, mapsConfigPath, time, weather);
    }

    protected void loadSpawns(Config config) {
        for (String rawCoords : config.getStringList(configPath + "spawns")) {
            this.spawns.add(Position.fromObject(
                YamlUtil.parseVector3Centered(rawCoords), 
                level
            ));
        }
        Collections.shuffle(spawns);
    }

    public Position getRandomSpawn() {
        if (spawns.isEmpty()) return null;
        if (spawnIndex >= spawns.size()) spawnIndex = 0;
        return spawns.get(spawnIndex++);
    }

    public List<Position> getSpawns() { return spawns; }

}