package org.brlnsreb.core.minigame.match;

public class MatchState {
    public MatchStateType current;

    public MatchState() {
        this.current = MatchStateType.WAITING_LOBBY;     //default value
    }
}
