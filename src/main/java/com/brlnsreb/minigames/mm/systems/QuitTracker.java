package com.brlnsreb.minigames.mm.systems;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// TODO: MAYBE quittracking astraction into Utils

public class QuitTracker {
    
    private final Set<UUID> quittedPlayers;
    
    public QuitTracker() {
        this.quittedPlayers = new HashSet<>();
    }
    
    public void markAsQuitted(UUID playerId) {
        quittedPlayers.add(playerId);
    }
    
    public boolean hasQuitted(UUID playerId) {
        return quittedPlayers.contains(playerId);
    }

    public void removePlayer(UUID playerId) {
        quittedPlayers.remove(playerId);
    }
    
    public void clear() {
        quittedPlayers.clear();
    }
}