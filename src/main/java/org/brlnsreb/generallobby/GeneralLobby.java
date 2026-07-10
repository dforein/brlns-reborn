package org.brlnsreb.generallobby;

import java.util.HashMap;
import java.util.Map;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.generallobby.items.MainLobbyItemManager;
import org.brlnsreb.generallobby.ui.MainLobbyBossBar;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.Server;
import org.powernukkitx.utils.Config;

public class GeneralLobby extends Lobby {

    public static GeneralLobby instance;

    private final MainLobbyBossBar bossBar;
    private static MainLobbyItemManager items;

    private final HashMap<NPCEntity, String> npcNameTagMap = new HashMap<>();

    public GeneralLobby() {
        super();
        instance = this;

        this.bossBar = new MainLobbyBossBar(messages.getString("name"));
        items = new MainLobbyItemManager(config);

        this.bossBar.startBossBarUpdates(this.level);
        this.spawnAllNpcs();
    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.LOBBY; 
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        items.giveGeneralLobbyItems(player);
    }

    private void spawnAllNpcs() {
        for (String gameNameTag : config.getStringList("npc.list")) {
            String configPath = "lobby.npc." + gameNameTag;
            Minigame minigame = MinigameManager.getMinigame(gameNameTag);

            NPCEntity npc = spawnNpc(
                configPath,
                (CustomPlayer player) -> { minigame.onLobbyJoin(player); },
                false
            );

            npcNameTagMap.put(npc, gameNameTag);

            Server.getInstance().getScheduler().scheduleRepeatingTask(BrlnsReb.getInstance(), 
                () -> {
                    updateNpcSubtitle(npc);
                }, 100
            );
        }
    }

    private void updateNpcSubtitle(NPCEntity npc) {
        String subtitle = YamlUtil.getStr(configPath() + "npc.text2", config).formatted(
            minigame.getNameTag(),
            minigame.getMainPendingMatch().getNumber(),
            minigame.getMainPendingMatch().getPlayers().size()
        );
        
        npc.updateSubtitle(subtitle);
    }

    public void onConfigReload() {
        super.onConfigReload();

        for (Map.Entry<NPCEntity, String> npcEntry : npcNameTagMap.entrySet()) {
            reloadNpcConfigData(
                npcEntry.getKey(), 
                configPath() + "npc." + npcEntry.getValue(),
                false
            );
            updateNpcSubtitle(npcEntry.getKey());
        }

        bossBar.onConfigReload(messages.getString("name"));
    }

    public static GeneralLobby getInstance() { return instance; }
    public Config getConfig() { 
        return ConfigManager.getConfig("general-lobby/config.yml");
    }
    public Config getMessages() {
        return ConfigManager.getConfig("general-lobby/messages.yml");
    }
    public String requireConfigPath() { return ""; }
    
}
