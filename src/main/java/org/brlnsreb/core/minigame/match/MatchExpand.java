package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Position;

public abstract class MatchExpand extends Match {

    protected EndLobby endLobby;
    
    public MatchExpand(Minigame minigame, int matchNumber) {
        super(minigame, matchNumber);
        this.endLobby = new EndLobby(this);
    }

    public void onEndLobbyJoin(CustomPlayer player) {
        players.remove(player);
        endLobby.onJoin(player);
    }

    public boolean onDeath(DamageCause cause, CustomPlayer victim, CustomPlayer killer) {
        Position deathPos = victim.getPosition();

        if (getGame().onDeath(cause, victim, killer)) {
            players.remove(victim);
            onEndLobbyJoin(victim);

            getGame().afterDeath(cause, deathPos, victim, killer);
            
            return true;
        }
        return false;
    }

    @Override
    public GameExpand getGame() { return (GameExpand) game; }

}
