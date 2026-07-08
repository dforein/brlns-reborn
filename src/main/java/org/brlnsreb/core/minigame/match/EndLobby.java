package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;

import cn.nukkit.utils.Config;

public class EndLobby extends Lobby {

    public EndLobby(MinigameMatchExpand match) {
        super(match);

        //play again NPC
        spawnNpc(
            "match" + configPath() + "npc.play-again.",
            (CustomPlayer) -> { minigame.onMatchJoin(CustomPlayer); }
        );

        //spectate NPC
        spawnNpc(
            "match" + configPath() + "npc.spectate.",
            (CustomPlayer) -> { match.onJoinAsSpectator(CustomPlayer); }
        );

        //return to lobby NPC
        spawnNpc(
            "match" + configPath() + "npc.return-to-lobby.",
            (CustomPlayer) -> { minigame.onLobbyJoin(CustomPlayer); }
        );
    }


    //join logic

    protected PlayerStateType onJoinState() {
        return PlayerStateType.END_LOBBY;
    }

    protected void onJoinBossBar(CustomPlayer player) {}
    protected void onJoinItems(CustomPlayer player) {}

    
    
    public Config getConfig() { return ConfigManager.getGlobalConfig(); }
    public Config getMessages() { return null; }
    public String requireConfigPath() { return "end-lobby."; }
}
