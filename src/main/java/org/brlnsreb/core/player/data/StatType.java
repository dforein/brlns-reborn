package org.brlnsreb.core.player.data;

public enum StatType {
    MATCHES_PLAYED(1),
    WINS(2),
    LOSSES(3),
    KILLS(4),
    DEATHS(5);

    public final int id;
    public static final int size = StatType.values().length;

    private StatType(int id) {
        this.id = id;
    }
}
