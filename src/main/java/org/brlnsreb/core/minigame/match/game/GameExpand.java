package org.brlnsreb.core.minigame.match.game;

import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.cloudburstmc.protocol.bedrock.data.actor.EntityDamageCause;

public abstract class GameExpand extends Game {

    public GameExpand(MatchExpand match, String mapId, TimeOfDay time, Weather weather) {
        super(match, mapId, time, weather);
    }

    public abstract void onDeath(EntityDamageCause cause, CustomPlayer victim, CustomPlayer killer);

    public MatchExpand getMatch() { return (MatchExpand) match; }
    
}
