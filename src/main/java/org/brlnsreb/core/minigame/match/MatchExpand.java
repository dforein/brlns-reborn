package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Location;

public abstract class MatchExpand extends Match {

    protected DeathLobby deathLobby;
    
    public MatchExpand(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
        this.deathLobby = new DeathLobby(this);
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
        deathLobby.close();
    }

    @Override
    public void onEnding() {
        super.onEnding();
        deathLobby.close();
    }

    @Override
    public GameExpand getGame() { return (GameExpand) game; }

}
