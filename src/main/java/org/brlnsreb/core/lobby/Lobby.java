package org.brlnsreb.core.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.lobby.entities.HologramEntity;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.YamlUtil;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Position;
import org.powernukkitx.utils.Config;

public abstract class Lobby {

    protected final Minigame minigame;
    protected final Match match;

    protected final Level level;
    protected Position spawnPos;

    protected Config config;
    protected Config messages;
    protected Map<NPCEntity, String> npcConfigPathMap = new HashMap<>();

    public Lobby(Minigame minigame, Match match) {
        this.minigame = minigame;
        this.match = match;

        this.config = getConfig();
        this.messages = getMessages();

        String levelPath = configPath() + "world";
        this.level = WorldManager.loadLobbyLevel(config.getString(levelPath));
        this.spawnPos = YamlUtil.parsePosition(config.getString(configPath() + "spawn-pos"), this.level);
    }

    public Lobby(Minigame minigame) {
        this(minigame, null);
    }

    public Lobby(Match match) {
        this(match.getMinigame(), match);
    }

    public Lobby() {
        this(null, null);
    }

    public boolean onJoin(CustomPlayer player) {
        PlayerUtils.changeWorld(player, spawnPos, true);

        onJoinMessages(player);

        player.setLobby(this);
        player.minigameCurrent = minigame;
        PlayerUtils.setLobbyState(player, onJoinState());

        onJoinBossBar(player);
        onJoinItems(player);

        player.matchCurrent = match;

        return true;
    }

    protected abstract PlayerStateType onJoinState();
    protected abstract void onJoinMessages(CustomPlayer player);    //chat, titles, etc.
    protected abstract void onJoinBossBar(CustomPlayer player);
    protected abstract void onJoinItems(CustomPlayer player);
    
    public void teleportToSpawn(CustomPlayer player) {
        PlayerUtils.lobbyTeleport(player, spawnPos);
    }

    protected void createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<CustomPlayer> task) {
        return spawnNpc(configPath, task, false);
    }

    protected NPCEntity spawnNpc(String configPath, Config customConfig, Consumer<CustomPlayer> task) {
        return spawnNpc(configPath, customConfig, task, false);
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<CustomPlayer> task, boolean subtitle) {
        return spawnNpc(configPath, this.config, task, subtitle);
    }

    protected NPCEntity spawnNpc(String configPath, Config customConfig, Consumer<CustomPlayer> task, boolean subtitle) {
        configPath = YamlUtil.checkConfigPath(configPath);
        Position pos = YamlUtil.parsePosition(customConfig.getString(configPath + "pos"), this.level);
        
        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));

        npc.updateTitle(customConfig.getString(configPath + "text1"));
        if (subtitle) npc.updateSubtitle(customConfig.getString(configPath + "text2"));

        npc.setDefaultPose(customConfig.getDouble(configPath + "default-yaw"));
        npc.setTask(task);
        npc.setSkin(customConfig.getString(configPath + "skin-file"));

        npc.spawnToAll();

        return npc;
    }

    public void close() {
        Map<Long, Player> players = level.getPlayers();
        if (!players.isEmpty()) {
            for (Player p : players.values()) {
                p.sendMessage(ChatMsgs.ERROR_PFX + "Something wrong happened, joining main hub...");
                MainHub.instance.onJoin((CustomPlayer) p);
            }
        }

        WorldManager.unloadLevel(this.level);
    }

    public void onConfigReload() {
        this.spawnPos = YamlUtil.parsePosition(config.getString("lobby.spawn"), this.level);
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, boolean subtitle) {
        reloadNpcConfigData(npc, configPath, this.config, subtitle);
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, Config customConfig, boolean subtitle) {
        if (npc == null) return;

        configPath = YamlUtil.checkConfigPath(configPath);
        
        npc.setDefaultPose(customConfig.getDouble(configPath + "default-yaw"));
        npc.updateTitle(customConfig.getString(configPath + "text1"));
        if (subtitle) {
            npc.updateSubtitle(customConfig.getString(configPath + "text2"));
        }
        npc.setSkin(customConfig.getString(configPath + "skin-file"));
    }

    public Level getLevel() { return this.level; }
    public abstract Config getConfig();
    public abstract Config getMessages();
    public abstract String requireConfigPath();
    public String configPath() { return YamlUtil.checkConfigPath(requireConfigPath()); }

}
