package org.brlnsreb.core.player.data;

public enum StatType {
    MATCHES_PLAYED(0),
    WINS(1),
    LOSSES(2),
    KILLS(3),
    DEATHS(4);

    public final int id;
    public static final int size = StatType.values().length;

    private StatType(int id) {
        this.id = id;
    }
}
