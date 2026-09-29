package org.brlnsreb.core.minigame;

public enum MinigameType {
    MURDER_MYSTERY(1, "mm", "§aMurder§2Mystery", "§aM§2M");

    public static final int size = MinigameType.values().length;

    public final int id;
    public final String nameTag;
    public final String displayName;
    public final String displayNameTagP;
    public final String displayNameTagY;
    public final String prefix;

    private MinigameType(int id, String nameTag, String displayName, String prefix) {
        this.id = id;
        this.nameTag = nameTag;
        this.displayName = "§l" + displayName;
        this.displayNameTagP = "§l§d" + nameTag.toUpperCase() + "§r";
        this.displayNameTagY = "§l§e" + nameTag.toUpperCase() + "§r";
        this.prefix = "§l" + prefix + " §r";
    }
    
    public static MinigameType fromId(int id) {
        for (MinigameType mg : values()) {
            if (mg.id == id) return mg;
        }
        return null;
    }

    public static MinigameType fromNameTag(String nameTag) {
        for (MinigameType mg : values()) {
            if (mg.nameTag.equals(nameTag)) return mg;
        }
        return null;
    }
}
