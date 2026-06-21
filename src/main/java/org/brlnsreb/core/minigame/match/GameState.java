package org.brlnsreb.core.minigame.match;

public class GameState {
    public GameStateType current;

    public GameState() {
        this.current = GameStateType.WAITING_LOBBY;     //default value
    }
}
