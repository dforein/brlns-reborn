package org.brlnsreb.core.minigame.match;

import org.brlnsreb.core.Configs;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.utils.YamlUtil;
import org.powernukkitx.utils.Config;

public class DeathLobby extends Lobby {

    public DeathLobby(MatchExpand match) {
        super(match);

        //play again NPC
        spawnNpc(
            configPath() + "npc.play-again.",
            player -> minigame.onMatchJoin(player)
        );

        //spectate NPC
        spawnNpc(
            configPath() + "npc.spectate.",
            player -> match.onJoinAsSpectator(player)
        );

        //return to lobby NPC
        spawnNpc(
            configPath() + "npc.return-to-lobby.",
            player -> minigame.onLobbyJoin(player)
        );
    }


    //join logic

    protected PlayerStateType onJoinState() {
        return PlayerStateType.DEATH_LOBBY;
    }

    protected void onJoinMessages(CustomPlayer player) {
        player.sendTitle(
            YamlUtil.getStr(requireConfigPath() + "title", config), 
            YamlUtil.getStr(requireConfigPath() + "subtitle", config),
            10, 60, 10
        );
    }

    protected void onJoinUi(CustomPlayer player) {}
    protected void onJoinItems(CustomPlayer player) {}

    
    
    public Config getConfig() { return Configs.getGlobalConfig(); }
    public Config getMessages() { return null; }
    public String requireConfigPath() { return "match.death-lobby."; }
}
