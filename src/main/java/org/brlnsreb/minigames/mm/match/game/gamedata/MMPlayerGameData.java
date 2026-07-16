package org.brlnsreb.minigames.mm.match.game.gamedata;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerGameData;

public class MMPlayerGameData extends PlayerGameData {

    public MMRole role = null;

    //innocents
    public int gold = 0;
    private static final int GOLD_SHERIFF = 5;

    //murderer
    public boolean flashUsed = false;

    public MMPlayerGameData(CustomPlayer player) {
        super(player);
    }

    public boolean canBecomeSheriff() {
        return role == MMRole.INNOCENT && gold >= GOLD_SHERIFF;
    }
    
}
