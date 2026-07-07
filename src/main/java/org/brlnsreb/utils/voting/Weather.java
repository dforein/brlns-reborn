package org.brlnsreb.utils.voting;

import cn.nukkit.level.Level;

public enum Weather {
    CLEAR("Clear"),
    RAIN("Rain"),
    STORM("Storm");

    public final String displayName;
    public final String name;
    
    private Weather(String displayName) {
        this.displayName = displayName;
        this.name = displayName.toLowerCase();
    }

    public static Weather get(String weatherName) {
        return switch (weatherName) {
            case "clear" -> CLEAR;
            case "rain" -> RAIN;
            case "storm" -> STORM;
            default -> null;
        };
    }

    public static void setWeather(Level level, Weather weather) {
        if (weather != null) {
            level.setRaining(weather != CLEAR);
            level.setThundering(weather == STORM);
        } else {
            level.setRaining(false);
            level.setThundering(false);
        }
    }

}