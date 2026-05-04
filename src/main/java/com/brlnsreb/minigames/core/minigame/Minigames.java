package com.brlnsreb.minigames.core.minigame;

public enum Minigames {
    MURDER_MYSTERY(1, "mm", true);

    private final int id;
    private final String nameTag;
    private final boolean pvp;

    private Minigames(int id, String nameTag, boolean pvp) {
        this.id = id;
        this.nameTag = nameTag;
        this.pvp = pvp;
    }

    public int getId() { return id; }
    public String getNameTag() { return nameTag; }
    public boolean isPvp() { return pvp; }
    
    public static Minigames fromId(int id) {
        for (Minigames mg : values()) {
            if (mg.id == id) return mg;
        }
        return null;
    }
}
