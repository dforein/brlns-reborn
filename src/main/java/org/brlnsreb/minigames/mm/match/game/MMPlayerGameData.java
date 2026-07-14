package org.brlnsreb.minigames.mm.match.game;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerGameData;
import org.brlnsreb.core.player.data.StatType;
import org.brlnsreb.minigames.mm.roles.MMRole;

public class MMPlayerGameData extends PlayerGameData {

    public MMRole role = null;

    //innocents
    public int gold = 0;
    private static final int GOLD_SHERIFF = 5;

    //murderer
    public boolean flashUsed = false;
    public boolean firstKill = false;

    public Boolean isWinner = null;

    public MMPlayerGameData(CustomPlayer player) {
        super(player);
    }

    public boolean canBecomeSheriff() {
        return role == MMRole.INNOCENT && gold >= GOLD_SHERIFF;
    }

    public void addStatOnEnding() {
        if (isWinner) incrementStat(StatType.WINS);
        else incrementStat(StatType.LOSSES);
    }
    
}
