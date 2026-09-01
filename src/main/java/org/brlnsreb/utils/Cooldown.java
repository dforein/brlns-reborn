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

    public static Cooldown ticks(double ticks) {
        return new Cooldown((int) (ticks * 50));
    }

    public static Cooldown milliseconds(int milliseconds) {
        return new Cooldown(milliseconds);
    }

    public boolean check(Object obj) {
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(obj)
            && now - cooldowns.get(obj) < milliseconds) {
            return false;
        }

        cooldowns.put(obj, now);
        return true;
    }

    public int getSecondsRemaining(Object obj) {
        if (!cooldowns.containsKey(obj)) return -1;
        return (int) ((milliseconds - System.currentTimeMillis() + cooldowns.get(obj)) / 1000);
    }
    
    public int getTicksRemaining(Object obj) {
        if (!cooldowns.containsKey(obj)) return -1;
        return (int) ((milliseconds - System.currentTimeMillis() + cooldowns.get(obj)) / 50);
    }
    public int getMillisecondsRemaining(Object obj) {
        if (!cooldowns.containsKey(obj)) return -1;
        return (int) (milliseconds - System.currentTimeMillis() + cooldowns.get(obj));
    }

    public int getSeconds(Object obj) {
        if (!cooldowns.containsKey(obj)) return -1;
        return (int) ((System.currentTimeMillis() - cooldowns.get(obj)) / 1000);
    }

    public int getTicks(Object obj) {
        if (!cooldowns.containsKey(obj)) return -1;
        return (int) ((System.currentTimeMillis() - cooldowns.get(obj)) / 50);
    }

    public int getMilliseconds(Object obj) {
        if (!cooldowns.containsKey(obj)) return -1;
        return (int) (System.currentTimeMillis() - cooldowns.get(obj));
    }

}
