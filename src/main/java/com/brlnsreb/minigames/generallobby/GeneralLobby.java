package com.brlnsreb.minigames.generallobby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.lobby.ui.MainLobbyBossBar;
import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.core.player.PlayerUtils;
import com.brlnsreb.minigames.generallobby.items.MainLobbyItemManager;
import com.brlnsreb.minigames.generallobby.ui.GamesMenu;
import com.brlnsreb.minigames.utils.YamlUtil;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;

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

        GamesMenu.init(config);
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateLobbyBossBar(player);
    }

    protected void onJoinItems(CustomPlayer player) {
        giveLobbyItems(player);
    }

    public static void giveLobbyItems(CustomPlayer player) {
        PlayerUtils.clearInventory(player);

        items.giveGames(player);
        items.giveMenu(player);
        items.giveMagicStaff(player);
        items.giveJoinGame(player);
    }

    private void spawnAllNpcs() {
        for (String gameNameTag : (List<String>) config.getList("npc.list")) {
            String configPath = "lobby.npc." + gameNameTag;
            Minigame minigame = MinigameManager.getMinigame(gameNameTag);

            NPCEntity npc = spawnNpc(
                configPath,
                (Player player) -> { minigame.onLobbyJoin(player); },
                false
            );

            npcNameTagMap.put(npc, gameNameTag);

            Server.getInstance().getScheduler().scheduleRepeatingTask(MinigameCore.getInstance(), 
                () -> {
                    updateNpcSubtitle(npc);
                }, 100
            );
        }
    }

    private void updateNpcSubtitle(NPCEntity npc) {
        String subtitle = YamlUtil.getStr(getConfigPath() + "npc.text2", config).formatted(
            minigame.getNameTag(),
            minigame.getMainPendingMatch().getNumber(),
            minigame.getMainPendingMatch().getPlayers().size()
        );
        
        npc.updateSubtitle(subtitle);
    }

    public void reloadConfig() {
        super.reloadConfig(true);

        for (Map.Entry<NPCEntity, String> npcEntry : npcNameTagMap.entrySet()) {
            reloadNpcConfigData(
                npcEntry.getKey(), 
                configPath() + "npc." + npcEntry.getValue(),
                false
            );
            updateNpcSubtitle(npcEntry.getKey());
        }

        bossBar.reloadConfig(messages.getString("name"));
    }

    public static GeneralLobby getInstance() { return instance; }
    public Config getNewConfig() { 
        return new Config(MinigameCore.getInstance().getDataFolder() + "general-lobby/config.yml", Config.YAML); 
    }
    public Config getNewMessages() {
        return new Config(MinigameCore.getInstance().getDataFolder() + "general-lobby/messages.yml", Config.YAML);
    }
    public String getConfigPath() { return ""; }
    
}
