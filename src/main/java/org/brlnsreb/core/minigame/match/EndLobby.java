package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;

import cn.nukkit.utils.Config;

public abstract class EndLobby extends Lobby {

    public EndLobby(MinigameMatchExpand match) {
        super(match);


    }
    
    public Config getConfig() { return ConfigManager.getConfig("global/config.yml"); }
    public Config getMessages() { return null; }
    public String requireConfigPath() { return "end-lobby."; }
}
