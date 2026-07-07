package org.brlnsreb.core.minigame.match.game;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.MinigameMatchExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public abstract class MinigameGameExpand extends MinigameGame {

    public MinigameGameExpand(MinigameMatch match, String map, TimeOfDay time, Weather weather) {
        super(match, map, time, weather);
    }

    public void onDeath(CustomPlayer player) {
        kill(player);
        ((MinigameMatchExpand) match).onDeath(player);
    }

    protected abstract void kill(CustomPlayer player);
    
}
