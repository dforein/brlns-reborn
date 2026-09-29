package org.brlnsreb.minigames.mm.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.MatchTeam;
import org.brlnsreb.core.minigame.match.game.Game;
import org.brlnsreb.core.minigame.match.game.GameTeam;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.waitinglobby.MMWaitingLobby;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;

public class MMMatch extends MatchExpand implements MatchTeam {

    public MMMatch(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
    }

    protected WaitingLobby createWaitingLobby() {
        return new MMWaitingLobby(this);
    }

    protected Game createGame(String map, TimeOfDay time, Weather weather) {
        weather = Weather.get(YamlUtil.getStr(
            "maps." + map + ".weather", 
            mapSettings
        ));
        
        return new MMGame(this, map, time, weather);
    }

    public GameTeam getTeamGame() {
        return (GameTeam) game;
    }

}
