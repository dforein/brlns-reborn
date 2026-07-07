package org.brlnsreb.minigames.mm.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.MinigameMatchExpand;
import org.brlnsreb.core.minigame.match.game.MinigameGame;
import org.brlnsreb.core.minigame.match.waitinglobby.WaitingLobby;
import org.brlnsreb.minigames.mm.match.waitinglobby.MMWaitingLobby;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;

public class MMMatch extends MinigameMatchExpand {

    public MMMatch(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
    }

    protected WaitingLobby createWaitingLobby() {
        return new MMWaitingLobby(this);
    }

    protected MinigameGame createGame(String map, TimeOfDay time, Weather weather) {
        return new MMGame(this, map, time, weather);
    }

}
