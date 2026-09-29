package org.brlnsreb.core.minigame.match.game;

import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Location;

public abstract class GameExpand extends Game {

    protected final MatchExpand match;

    public GameExpand(MatchExpand match, String mapId, TimeOfDay time, Weather weather) {
        super(match, mapId, time, weather);
        this.match = match;
    }

    public abstract boolean onDeath(DamageCause cause, CustomPlayer victim, CustomPlayer killer);   //return true = tp to end lobby
    public abstract void afterDeath(DamageCause cause, Location deathLoc, CustomPlayer victim, CustomPlayer killer);

    public MatchExpand getMatch() { return (MatchExpand) match; }
    
}
