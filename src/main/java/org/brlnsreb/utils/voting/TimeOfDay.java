package org.brlnsreb.utils.voting;

import org.powernukkitx.level.Level;

public enum TimeOfDay {
    DAY("Day", 6000),
    SUNSET("Sunset", 12000),
    NIGHT("Night", 18000),
    MIDNIGHT("Midnight", 20000),;

    public final String displayName;
    public final int timeValue;

    private TimeOfDay(String displayName, int timeValue) {
        this.displayName = displayName;
        this.timeValue = timeValue;
    }

    public static TimeOfDay get(String timeName) {
        return switch (timeName.toLowerCase()) {
            case "day" -> DAY;
            case "sunset" -> SUNSET;
            case "night" -> NIGHT;
            case "midnight" -> MIDNIGHT;
            default -> null;
        };
    }

    public static TimeOfDay setTime(Level level, TimeOfDay time) {
        if (time != null) {
            level.setTime(time.timeValue);
            return time;
        } else {
            level.setTime(DAY.timeValue);
            return DAY;
        }
    }
}
