package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.deathlobby.DeathLobby;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Location;

public abstract class MatchExpand extends Match {

    protected DeathLobby deathLobby = null;
    
    public MatchExpand(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
    }

    @Override
    public void loadGame(String mapId, TimeOfDay time, Weather weather) {
        if (deathLobby == null) deathLobby = new DeathLobby(this);
        super.loadGame(mapId, time, weather);
    }

    public boolean onDeath(CustomPlayer victim, CustomPlayer killer) { 
        return onDeath(null, victim, killer); 
    }
    
    public boolean onDeath(DamageCause cause, CustomPlayer victim, CustomPlayer killer) {
        Location deathLoc = victim.getLocation();

        if (getGame().onDeath(cause, victim, killer)) {
            players.remove(victim);
            deathLobby.onJoin(victim);
            game.prepareAndSaveData(victim, true);

            getGame().afterDeath(cause, deathLoc, victim, killer);
            
            return true;
        }
        return false;
    }

    @Override
    public void forceStop() {
        super.forceStop();
    }

    @Override
    public void onEnding() {
        super.onEnding();
    }

    @Override
    public GameExpand getGame() { return (GameExpand) game; }

}
