package org.brlnsreb.core.minigame;

import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.generallobby.items.MainLobbyItemManager;
import org.brlnsreb.generallobby.ui.MainLobbyBossBar;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.utils.Config;

public abstract class MinigameLobby extends Lobby {

    protected final NPCEntity joinNpc;

    protected MainLobbyBossBar bossBar;

    public MinigameLobby(Minigame minigame) {
        super(minigame);
        this.joinNpc = spawnJoinNpc(minigame);

        //leave npc
        spawnNpc(
            configPath() + "npc.",
            player -> GeneralLobby.instance.onJoin(player)
        );

        this.bossBar = new MainLobbyBossBar(config.getString("name"));
        this.bossBar.startBossBarUpdates(level);
    }


    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
    }
    
    protected void onJoinMessages(CustomPlayer player) {
        player.sendTitle(minigame.mgt.displayName, "§eplay.brlns.reb");
        GeneralLobby.friendAlertsNotify(player, minigame, minigame.mgt.displayName);
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        MainLobbyItemManager.getInstance().giveMinigameLobbyItems(player);
    }


    public void onReplaceMainPendingMatch(int matchNumber) {
        updateJoinNpcSubtitle();
    }

    private NPCEntity spawnJoinNpc(Minigame minigame) {
        String configPath = configPath() + "npc.";

        NPCEntity npc = spawnNpc(
            configPath,
            player -> minigame.onMatchJoin(player),
            false
        );

        return npc;
    }

    private void updateJoinNpcSubtitle() {
        String subtitle = YamlUtil.getStr(configPath() + "npc.text2", config).formatted(
            minigame.mgt.nameTag,
            minigame.getMainPendingMatch().getNumber(),
            minigame.getMainPendingMatch().getPlayers().size()
        );
        
        joinNpc.updateSubtitle(subtitle);
    }

    public void onConfigReload() {
        super.onConfigReload();

        reloadNpcConfigData(
            joinNpc, 
            configPath() + "npc.", 
            false
        );
    }

    public Config getConfig() { return minigame.getConfig(); }
    public Config getMessages() { return minigame.getMessages(); }
    public String requireConfigPath() { return "lobby."; }

}
