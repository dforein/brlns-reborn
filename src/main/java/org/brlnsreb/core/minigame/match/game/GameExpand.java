package org.brlnsreb.core.minigame.match.game;

import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public abstract class GameExpand extends Game {

    public GameExpand(MatchExpand match, String map, TimeOfDay time, Weather weather) {
        super(match, map, time, weather);
    }

    public void onDeath(CustomPlayer player) {
        kill(player);
        ((MatchExpand) match).onDeath(player);
    }

    protected abstract void kill(CustomPlayer player);
    
}
