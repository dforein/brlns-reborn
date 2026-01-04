package com.brlnsreb.minigames.mm.systems;

import java.util.HashSet;
import java.util.Set;

public class QuitTracker {
    
    private final Set<String> quittedPlayers;
    
    public QuitTracker() {
        this.quittedPlayers = new HashSet<>();
    }
    
    public void markAsQuitted(String playerName) {
        quittedPlayers.add(playerName);
    }
    
    public boolean hasQuitted(String playerName) {
        return quittedPlayers.contains(playerName);
    }
    
    public void clear() {
        quittedPlayers.clear();
    }
}