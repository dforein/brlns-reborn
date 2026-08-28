package org.brlnsreb.core.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.brlnsreb.core.lobby.entities.HologramEntity;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.maps.LobbyLevel;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.YamlUtil;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.level.Position;
import org.powernukkitx.utils.Config;

public abstract class Lobby {

    protected final Minigame minigame;
    protected final Match match;

    protected final LobbyLevel map;

    protected Config config;
    protected Config messages;
    protected Map<NPCEntity, String> npcConfigPathMap = new HashMap<>();

    public Lobby(Minigame minigame) {
        this(minigame, null);
    }

    public Lobby(Match match) {
        this(match.getMinigame(), match);
    }

    public Lobby() {
        this(null, null);
    }

    public Lobby(Minigame minigame, Match match) {
        this.minigame = minigame;
        this.match = match;

        this.config = getConfig();
        this.messages = getMessages();

        this.map = new LobbyLevel(this, match != null);
    }


    public boolean onJoin(CustomPlayer player) {
        PlayerStateType oldState = PlayerUtils.changeWorld(player, map.spawn, true);

        onJoinMessages(player);

        player.setLobby(this);
        player.minigameCurrent = minigame;
        player.matchCurrent = match;
        PlayerUtils.setLobbyState(player, oldState, onJoinState());

        player.waitForAck(() -> {
            onJoinUi(player);
            onJoinItems(player);
        });

        return true;
    }

    protected abstract PlayerStateType onJoinState();
    protected abstract void onJoinMessages(CustomPlayer player);    //chat, titles, etc.
    protected abstract void onJoinUi(CustomPlayer player);
    protected abstract void onJoinItems(CustomPlayer player);
    
    public void teleportToSpawn(CustomPlayer player) {
        PlayerUtils.lobbyTeleport(player, map.spawn);
    }


    protected HologramEntity createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
        return holo;
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<CustomPlayer> task) {
        return spawnNpc(configPath, task, false);
    }

    protected NPCEntity spawnNpc(String configPath, Config customConfig, Consumer<CustomPlayer> task) {
        return spawnNpc(configPath, customConfig, task, false);
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<CustomPlayer> task, boolean fixedSubtitle) {
        return spawnNpc(configPath, this.config, task, fixedSubtitle);
    }

    protected NPCEntity spawnNpc(String configPath, Config customConfig, Consumer<CustomPlayer> task, boolean fixedSubtitle) {
        configPath = YamlUtil.checkConfigPath(configPath);
        
        Position pos = YamlUtil.parsePositionCentered(customConfig.getString(configPath + "pos"), map.level);
        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));

        npc.updateTitle(customConfig.getString(configPath + "text1"));
        if (fixedSubtitle) npc.updateSubtitle(customConfig.getString(configPath + "text2"));

        npc.setDefaultPose(customConfig.getDouble(configPath + "default-yaw"));
        npc.setTask(task);
        npc.setSkin(customConfig.getString(configPath + "skin-file"));

        npc.spawnToAll();

        return npc;
    }


    public void close() {
        Map<Long, Player> players = map.getPlayers();
        if (!players.isEmpty()) {
            for (Player p : players.values()) {
                MainHub.instance.onJoin((CustomPlayer) p);
            }
        }

        map.close();
    }


    public void onConfigReload() {
        map.spawn = YamlUtil.parseLocationCentered(
            config.getString(configPath() + "spawn-pos"), 
            map.level,
            config.getInt(configPath() + "spawn-yaw")
        );
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, boolean fixedSubtitle) {
        reloadNpcConfigData(npc, configPath, this.config, fixedSubtitle);
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, Config customConfig, boolean fixedSubtitle) {
        if (npc == null) return;

        configPath = YamlUtil.checkConfigPath(configPath);
        
        npc.setDefaultPose(customConfig.getDouble(configPath + "default-yaw"));
        npc.updateTitle(customConfig.getString(configPath + "text1"));
        if (fixedSubtitle) npc.updateSubtitle(customConfig.getString(configPath + "text2"));
        npc.setSkin(customConfig.getString(configPath + "skin-file"));
    }
    

    public LobbyLevel getMap() { return map; };
    public abstract Config getConfig();
    public abstract Config getMessages();
    protected abstract String requireConfigPath();
    public String configPath() { return YamlUtil.checkConfigPath(requireConfigPath()); }

}
