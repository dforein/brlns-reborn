package org.brlnsreb.core.minigame;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.lobby.ui.MainLobbyBossBar;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;

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

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
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
            (CustomPlayer player) -> { minigame.onMatchJoin(player); },
            false
        );

        Server.getInstance().getScheduler().scheduleRepeatingTask(BrlnsReb.getInstance(), 
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
        super.reloadConfig();

        reloadNpcConfigData(
            joinNpc, 
            configPath() + "npc.", 
            false
        );
        updateJoinNpcSubtitle();
    }

    public Config getNewConfig() { return minigame.getConfig(); }
    public Config getNewMessages() { return minigame.getMessages(); }
    public String requireConfigPath() { return "lobby."; }

}
