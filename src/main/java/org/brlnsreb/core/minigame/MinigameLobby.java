package org.brlnsreb.core.minigame;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.generallobby.items.MainLobbyItemManager;
import org.brlnsreb.generallobby.ui.MainLobbyBossBar;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.Server;
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
            (CustomPlayer player) -> { GeneralLobby.instance.onJoin(player); }
        );

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
        MainLobbyItemManager.getInstance().giveMinigameLobbyItems(player);
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

    public void onConfigReload() {
        super.onConfigReload();

        reloadNpcConfigData(
            joinNpc, 
            configPath() + "npc.", 
            false
        );
        updateJoinNpcSubtitle();
    }

    public Config getConfig() { return minigame.getConfig(); }
    public Config getMessages() { return minigame.getMessages(); }
    public String requireConfigPath() { return "lobby."; }

}
