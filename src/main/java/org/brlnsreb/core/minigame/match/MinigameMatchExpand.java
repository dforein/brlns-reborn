package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.minigame.MinigameType;

public abstract class MinigameMatchExpand extends MinigameMatch {

    protected EndLobby endLobby;
    
    public MinigameMatchExpand(MinigameType minigame, int matchNumber) {
        super(minigame, matchNumber);
        createEndLobby("end-lobby");
    }

    protected abstract void createEndLobby(String configPath);

}
