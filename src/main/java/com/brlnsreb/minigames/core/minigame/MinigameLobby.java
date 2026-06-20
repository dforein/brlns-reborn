package com.brlnsreb.minigames.core.minigame;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.lobby.ui.MainLobbyBossBar;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.generallobby.GeneralLobby;
import com.brlnsreb.minigames.utils.YamlUtil;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;

public abstract class MinigameLobby extends Lobby {

    protected final NPCEntity joinNpc;
    protected MainLobbyBossBar bossBar;
    
    public MinigameLobby(Minigame minigame) {
        super(minigame);
        this.joinNpc = spawnJoinNpc(minigame);
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
        updateJoinNpcSubtitle();
    }

    private NPCEntity spawnJoinNpc(Minigame minigame) {
        String configPath = configPath() + "npc.";

        NPCEntity npc = spawnNpc(
            configPath,
            (Player player) -> { minigame.onMatchJoin(player); },
            false
        );

        Server.getInstance().getScheduler().scheduleRepeatingTask(MinigameCore.getInstance(), 
            () -> {
                updateJoinNpcSubtitle();
            }, 100
        );

        return npc;
    }

    private void updateJoinNpcSubtitle() {
        String subtitle = YamlUtil.getStr(configPath() + "npc.text2", config).formatted(
            minigame.getNameTag(),
            minigame.getMainPendingMatch().getNumber(),
            minigame.getMainPendingMatch().getPlayers().size()
        );
        
        joinNpc.updateSubtitle(subtitle);
    }

    public void reloadConfig() {
        super.reloadConfig(false);

        reloadNpcConfigData(
            joinNpc, 
            configPath() + "npc.", 
            false
        );
        updateJoinNpcSubtitle();
    }

    public Config getNewConfig() { return minigame.getConfig(); }
    public Config getNewMessages() { return minigame.getMessages(); }
    public String getConfigPath() { return "lobby."; }

}
