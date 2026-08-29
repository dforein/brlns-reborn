package org.brlnsreb.minigames.mm.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.game.Game;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.waitinglobby.MMWaitingLobby;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;

public class MMMatch extends MatchExpand {

    public MMMatch(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
    }

    protected WaitingLobby createWaitingLobby() {
        return new MMWaitingLobby(this);
    }

    protected Game createGame(String map, TimeOfDay time, Weather weather) {
        weather = Weather.get(YamlUtil.getStr(
            "map-settings.maps." + map + ".weather", 
            config
        ));
        
        return new MMGame(this, map, time, weather);
    }

}
