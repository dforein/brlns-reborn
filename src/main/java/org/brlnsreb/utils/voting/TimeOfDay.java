package org.brlnsreb.utils.voting;

import cn.nukkit.level.Level;

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

    public static void setTime(Level level, TimeOfDay time) {
        if (time != null) {
            level.setTime(time.timeValue);
        } else {
            level.setTime(DAY.timeValue);
        }
    }
}
