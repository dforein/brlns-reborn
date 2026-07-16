package org.brlnsreb.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown {
    
    private final Map<Object, Long> cooldowns = new ConcurrentHashMap<>();
    private final int milliseconds;

    public Cooldown(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public static Cooldown seconds(double seconds) {
        return new Cooldown((int) (seconds * 1000));
    }

    public static Cooldown ticks(int ticks) {
        return new Cooldown(ticks * 50);
    }

    public static Cooldown milliseconds(int milliseconds) {
        return new Cooldown(milliseconds);
    }

    public boolean check(Object obj) {
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(obj)
            && now - cooldowns.get(obj) < milliseconds) {
            return true;
        }

        cooldowns.put(obj, now);
        return false;
    }
}
