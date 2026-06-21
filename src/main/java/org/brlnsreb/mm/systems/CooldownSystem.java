package org.brlnsreb.mm.systems;

import java.util.HashMap;
import java.util.Map;

// TODO: cooldownsystem astraction/move into Utils
// TODO: optimize

public class CooldownSystem {
    
    private final Map<String, Long> cooldowns;
    
    public CooldownSystem() {
        this.cooldowns = new HashMap<>();
    }
    
    public boolean canUse(String key, double cooldownSeconds) {
        if (!cooldowns.containsKey(key)) {
            return true;
        }
        
        long lastUse = cooldowns.get(key);
        long now = System.currentTimeMillis();
        return (now - lastUse) >= (cooldownSeconds * 1000);
    }
    
    public void recordUse(String key) {
        cooldowns.put(key, System.currentTimeMillis());
    }
    
    public void clear() {
        cooldowns.clear();
    }
}