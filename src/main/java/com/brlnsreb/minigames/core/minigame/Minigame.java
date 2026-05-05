package com.brlnsreb.minigames.core.minigame;

import java.util.HashSet;

public abstract class Minigame {
    
    protected final int id;
    protected final String nameTag;
    protected final HashSet<? extends MinigameMatch> matches;
    protected int currentPlayers = 0;

    public Minigame(MinigameType minigame) {
        this.matches = new HashSet<>();
        this.id = minigame.getId();
        this.nameTag = minigame.getNameTag();
    }

    public abstract void createMatch();

    public HashSet<? extends MinigameMatch> getMatches() {
        return matches;
    }

    public int getId() {
        return id;
    }

    public String getNameTag() {
        return nameTag;
    }

    public abstract String getMessagePrefix();

}
