package com.brlnsreb.minigames.core.minigame;

import java.util.HashSet;

public abstract class Minigame {
    
    protected final int id;
    protected final String nameTag;
    protected HashSet<MinigameMatch> matches;

    public Minigame(Minigames minigame) {
        this.matches = new HashSet<>();
        this.id = minigame.getId();
        this.nameTag = minigame.getNameTag();
    }

    public abstract void createMatch();

    public HashSet<MinigameMatch> getMatches() {
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
