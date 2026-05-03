package com.brlnsreb.minigames.core.minigame;

import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import java.util.List;
import java.util.ArrayList;

public class Arena {
    
    private final String name;
    private final Level level;
    private final Vector3 min;
    private final Vector3 max;
    private final List<Vector3> spawns;
    
    public Arena(String name, Level level, Vector3 min, Vector3 max, List<Vector3> spawns) {
        this.name = name;
        this.level = level;
        this.min = min;
        this.max = max;
        this.spawns = new ArrayList<>(spawns);
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
        return new ArrayList<>(spawns);
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