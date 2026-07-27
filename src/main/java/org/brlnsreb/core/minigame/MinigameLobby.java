package org.brlnsreb.core.minigame;

import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.mainhub.items.MainLobbyItemManager;
import org.brlnsreb.mainhub.ui.MainLobbyBossBar;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.utils.Config;

public abstract class MinigameLobby extends Lobby {

    protected final NPCEntity joinNpc;
    protected final NPCEntity backToHubNpc;

    protected MainLobbyBossBar bossBar;

    public MinigameLobby(Minigame minigame) {
        super(minigame);

        this.joinNpc = spawnNpc(
            configPath() + "npc.join.",
            player -> minigame.onMatchJoin(player),
            false
        );

        this.backToHubNpc = spawnNpc(
            configPath() + "npc.back-to-hub.",
            player -> MainHub.instance.onJoin(player),
            false
        );
        BrlnsReb.getScheduler().scheduleRepeatingTask(BrlnsReb.instance, 
            () -> updateBackToHubNpcSubtitle(), 
            ThreadLocalRandom.current().nextInt(190, 210)
        );

        this.bossBar = new MainLobbyBossBar(minigame.mgt.displayName);
        this.bossBar.startBossBarUpdates(level);
    }


    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
    }
    
    protected void onJoinMessages(CustomPlayer player) {
        player.sendTitle(minigame.mgt.displayName, "§eplay.brlns.reb");
        MainHub.friendAlertsNotify(player, minigame, minigame.mgt.displayName);
    }

    protected void onJoinUi(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        MainLobbyItemManager.instance.giveMinigameLobbyItems(player);
    }


    public void updateJoinNpcSubtitle() {
        Match mainPendingMatch = minigame.getMainPendingMatch();

        String subtitle = YamlUtil.getStr(configPath() + "npc.join.text2", config).formatted(
            minigame.mgt.nameTag,
            mainPendingMatch.getNumber(),
            mainPendingMatch.getPlayers().size(),
            mainPendingMatch.getWaitingLobby().getMaxPlayers()
        );
        
        joinNpc.updateSubtitle(subtitle);
    }
 
    private void updateBackToHubNpcSubtitle() {
        String subtitle = YamlUtil.getStr(configPath() + "npc.back-to-hub.text2", config)
            .formatted(MainHub.onlinePlayers);

        backToHubNpc.updateSubtitle(subtitle);
    }


    public void onConfigReload() {
        super.onConfigReload();
        
        joinNpc.blockNpc(7);
        backToHubNpc.blockNpc(7);

        reloadNpcConfigData(
            joinNpc, 
            configPath() + "npc.join.", 
            false
        );

        reloadNpcConfigData(
            backToHubNpc, 
            configPath() + "npc.back-to-hub.", 
            false
        );
    }


    public Config getConfig() { return minigame.getConfig(); }
    public Config getMessages() { return minigame.getMessages(); }
    public String requireConfigPath() { return "lobby."; }

}
