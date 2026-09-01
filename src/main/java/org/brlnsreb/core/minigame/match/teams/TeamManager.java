package org.brlnsreb.core.minigame.match.teams;

import java.util.ArrayList;

import org.brlnsreb.core.player.CustomPlayer;

public interface TeamManager {

    public ArrayList<CustomPlayer> getTeam(CustomPlayer player);

}