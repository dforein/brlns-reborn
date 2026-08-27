package org.brlnsreb.core.maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.level.Location;
import org.powernukkitx.level.Position;
import org.powernukkitx.utils.Config;

public class RandomSpawnsMap extends GameMapLevel {

    private final List<Position> spawns = new ArrayList<>();
    private int spawnIndex = 0;

    private final Map<UUID, Location> playerSpawns = new HashMap<>();
    
    public RandomSpawnsMap(Config config, String mapId, String mapsConfigPath, TimeOfDay time, Weather weather) {
        super(config, mapId, mapsConfigPath, time, weather);
    }

    protected void loadSpawns() {
        for (String rawCoords : config.getStringList(configPath + "spawns")) {
            spawns.add(Position.fromObject(
                YamlUtil.parseVector3Centered(rawCoords), 
                level
            ));
        }
        Collections.shuffle(spawns);
    }

    public Location getSpawnFor(CustomPlayer player) {
        if (spawns.isEmpty()) return null;

        UUID uuid = player.getUniqueId();
        if (playerSpawns.containsKey(uuid)) {
            return playerSpawns.get(uuid);
        }

        if (spawnIndex >= spawns.size()) spawnIndex = 0;
        Location spawnLoc = spawns.get(spawnIndex++).getLocation();
        playerSpawns.put(uuid, spawnLoc);
        return spawnLoc;
    }

    public List<Position> getSpawns() { return spawns; }

}