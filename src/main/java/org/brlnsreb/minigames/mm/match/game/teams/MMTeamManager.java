package org.brlnsreb.minigames.mm.match.game.teams;

import java.util.ArrayList;

import org.brlnsreb.core.minigame.match.teams.TeamManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;

public class MMTeamManager implements TeamManager {

    private final MMGame game;

    public MMTeamManager(MMGame game) {
        this.game = game;
    }

    public ArrayList<CustomPlayer> getTeam(CustomPlayer player) {
        CustomPlayer murderer = game.getMurderer();
        CustomPlayer sheriff = game.getSheriff();
        ArrayList<CustomPlayer> team;

        if (player == murderer || player == sheriff) {
            team = new ArrayList<>();
            team.add(player);
            return team;
        }
        
        team = new ArrayList<>(game.getPlayers());
        team.remove(murderer);
        team.remove(sheriff);
        return team;
    }
    
}
