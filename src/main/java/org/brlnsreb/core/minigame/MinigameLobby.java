package org.brlnsreb.core.minigame;

import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.HologramEntity;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.mainhub.items.MainLobbyItemManager;
import org.brlnsreb.mainhub.ui.MainLobbyBossBar;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.utils.Config;

public abstract class MinigameLobby extends Lobby {

    protected final NPCEntity joinNpc;
    protected final NPCEntity backToHubNpc;
    protected final HologramEntity mainHolo;

    protected final MainLobbyBossBar bossBar;

    public MinigameLobby(Minigame minigame) {
        super(minigame);

        //npcs

        this.joinNpc = spawnNpc(
            "join",
            player -> minigame.onMatchJoin(player),
            false
        );

        this.backToHubNpc = spawnNpc(
            "back-to-hub",
            player -> MainHub.instance.onJoin(player),
            false
        );
        BrlnsReb.getScheduler().scheduleRepeatingTask(BrlnsReb.instance, 
            () -> updateBackToHubNpcSubtitle(), 
            ThreadLocalRandom.current().nextInt(190, 210)
        );

        //holos

        this.mainHolo = createHologram("main");
        BrlnsReb.getScheduler().scheduleRepeatingTask(BrlnsReb.instance, 
            () -> updateMainHolo(), 
            ThreadLocalRandom.current().nextInt(190, 210)
        );

        //bossbar

        this.bossBar = new MainLobbyBossBar(minigame.mgt.displayName);
        this.bossBar.startBossBarUpdates(map.level);
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

        String subtitle = YamlUtil.getStr(configPath() + "npcs.join.text2", config).formatted(
            minigame.mgt.nameTag,
            mainPendingMatch.getNumber(),
            mainPendingMatch.getPlayers().size(),
            mainPendingMatch.getWaitingLobby().getMaxPlayers()
        );
        
        joinNpc.updateSubtitle(subtitle);
    }
 
    private void updateBackToHubNpcSubtitle() {
        String subtitle = YamlUtil.getStr(configPath() + "npcs.back-to-hub.text2", config)
            .formatted(MainHub.onlinePlayers);

        backToHubNpc.updateSubtitle(subtitle);
    }

    private void updateMainHolo() {
        String text = YamlUtil.getStr("lobby.holograms.main.text", Configs.getGlobalConfig()).formatted(
            ChatMsgs.BROKENLENS_GAMES,
            minigame.mgt.displayNameTagY,
            MainHub.onlinePlayers
        );

        mainHolo.setText(text);
    }


    public void onConfigReload() {
        super.onConfigReload();
        
        joinNpc.tempBlockTask(7);
        backToHubNpc.tempBlockTask(7);

        reloadNpcConfigData(joinNpc, "join", false);
        reloadNpcConfigData(backToHubNpc, "back-to-hub", false);

        reloadHologramConfigData(mainHolo, "main");
    }


    public Config getConfig() { return minigame.getConfig(); }
    public Config getMessages() { return minigame.getMessages(); }
    public String requireConfigPath() { return "lobby."; }

}
