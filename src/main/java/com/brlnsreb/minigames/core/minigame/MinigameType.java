package com.brlnsreb.minigames.core.minigame;

public enum MinigameType {
    MURDER_MYSTERY(1, "mm", true);

    private final int id;
    private final String nameTag;
    private final boolean pvp;

    private MinigameType(int id, String nameTag, boolean pvp) {
        this.id = id;
        this.nameTag = nameTag;
        this.pvp = pvp;
    }

    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    public boolean isPvp() { return pvp; }
    
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
