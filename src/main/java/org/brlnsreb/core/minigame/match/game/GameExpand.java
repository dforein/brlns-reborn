package org.brlnsreb.core.minigame.match.game;

import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Position;

public abstract class GameExpand extends Game {

    public GameExpand(MatchExpand match, String mapId, TimeOfDay time, Weather weather) {
        super(match, mapId, time, weather);
    }

    public abstract boolean onDeath(DamageCause cause, CustomPlayer victim, CustomPlayer killer);   //return true = tp to end lobby
    public abstract void afterDeath(DamageCause cause, Position deathPos, CustomPlayer victim, CustomPlayer killer);

    public MatchExpand getMatch() { return (MatchExpand) match; }
    
}
