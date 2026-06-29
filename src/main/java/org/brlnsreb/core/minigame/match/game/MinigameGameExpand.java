package org.brlnsreb.core.minigame.match.game;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.MinigameMatchExpand;
import org.brlnsreb.core.player.CustomPlayer;

public abstract class MinigameGameExpand extends MinigameGame {

    public MinigameGameExpand(MinigameMatch match, String map) {
        super(match, map);
    }

    public void onDeath(CustomPlayer player) {
        ((MinigameMatchExpand) match).onDeath(player);
    }
    
}
