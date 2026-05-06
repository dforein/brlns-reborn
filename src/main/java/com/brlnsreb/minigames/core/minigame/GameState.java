package com.brlnsreb.minigames.core.minigame;

public class GameState {
    public GameStateType current;

    public GameState() {
        this.current = GameStateType.WAITING_LOBBY;     //default value
    }
}
