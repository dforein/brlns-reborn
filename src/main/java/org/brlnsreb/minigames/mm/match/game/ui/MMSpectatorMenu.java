package org.brlnsreb.minigames.mm.match.game.ui;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.utils.abstraction.SpectatorMenuAbstract;

public class MMSpectatorMenu extends SpectatorMenuAbstract {

    private final MMGame game;
    
    public MMSpectatorMenu(MMGame game) {
        super();
        this.game = game;
    }

    protected String getDisplayNameForSpectateMenu(CustomPlayer player) {
        MMPlayerGameData gameData = game.getGameData(player);
        if (gameData == null) return null;

        return player.data.name 
            + switch (gameData.role) {
                case SHERIFF -> " §8(§l§9SHERIFF§r§8)";
                case MURDERER -> " §8(§l§cMURDERER§r§8)";
                case INNOCENT -> " §8(§l§aINNOCENT§r§8)";
            };
    }

    protected MMGame getGame() {
        return game;
    }
    
}