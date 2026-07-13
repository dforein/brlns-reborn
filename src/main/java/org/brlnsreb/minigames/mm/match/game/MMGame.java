package org.brlnsreb.minigames.mm.match.game;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public class MMGame extends GameExpand {

    public MMGame(MatchExpand match, String map, TimeOfDay time, Weather weather) {
        super(match, map, time, weather);
    }

    
    
}
