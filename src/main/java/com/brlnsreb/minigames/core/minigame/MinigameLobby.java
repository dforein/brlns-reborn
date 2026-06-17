package com.brlnsreb.minigames.core.minigame;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.lobby.ui.MainLobbyBossBar;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.generallobby.GeneralLobby;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

public abstract class MinigameLobby extends Lobby {

    protected final NPCEntity joinNpc;
    protected final String joinNpcSubtitle;
    protected MainLobbyBossBar bossBar;
    
    public MinigameLobby(Minigame minigame) {
        super(minigame);
        this.joinNpc = spawnJoinNpc(minigame);
        this.joinNpcSubtitle = config.getString("npc.subtitle");
        this.bossBar = new MainLobbyBossBar(config.getString("name"));

        this.bossBar.startBossBarUpdates(level);
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        GeneralLobby.giveLobbyItems(player);
    }

    public void onReplaceMainPendingMatch(int matchNumber) {
        joinNpc.updateSubtitle(
            joinNpcSubtitle.formatted(minigame.getNameTag(), matchNumber)
        );
    }

    private NPCEntity spawnJoinNpc(Minigame minigame) {
        return spawnNpc(
            "lobby.npc.",
            (Player player) -> { minigame.onMatchJoin(player); },
            true, minigame
        );
    }

    public void reloadConfig() {
        super.reloadConfig(false);
        reloadNpcConfigData(joinNpc, "lobby.npc.", true, true);
    }

    public Config getNewConfig() { return minigame.getConfig(); }
    public Config getNewMessages() { return minigame.getMessages(); }
    public String getConfigPath() { return "lobby."; }

}
