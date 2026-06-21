package org.brlnsreb.core.minigame;

public enum MinigameType {
    MURDER_MYSTERY(1, "mm");

    private final int id;
    private final String nameTag;

    private MinigameType(int id, String nameTag) {
        this.id = id;
        this.nameTag = nameTag;
    }

    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    
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
