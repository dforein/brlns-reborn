package com.brlnsreb.minigames.core.database;

public enum StatType {
    MATCHES_PLAYED(1),
    WINS(2),
    KILLS(3);

    public final int id;

    private StatType(int id) {
        this.id = id;
    }
}
