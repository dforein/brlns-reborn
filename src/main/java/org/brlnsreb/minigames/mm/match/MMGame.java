package org.brlnsreb.minigames.mm.match;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.game.MinigameGameExpand;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public class MMGame extends MinigameGameExpand {

    public MMGame(MinigameMatch match, String map, TimeOfDay time, Weather weather) {
        super(match, map, time, weather);
    }
    
}
