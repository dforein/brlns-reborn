package org.brlnsreb.core.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.lobby.entities.HologramEntity;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

public abstract class Lobby {

    protected final Minigame minigame;
    protected final MinigameMatch match;

    protected final Level level;
    protected Position spawnPos;

    protected Config config;
    protected Config messages;
    protected Map<NPCEntity, String> npcConfigPathMap = new HashMap<>();

    public Lobby(Minigame minigame, MinigameMatch match) {
        this.minigame = minigame;
        this.match = match;

        String levelPath = configPath().equals("") ?
            "world" : configPath() + ".world";
        this.level = WorldManager.loadLobbyLevel(config.getString(levelPath));
        this.spawnPos = YamlUtil.parsePosition(config.getString(configPath() + "spawn"), this.level);

        this.config = getConfig();
        this.messages = getMessages();
    }

    public Lobby(Minigame minigame) {
        this(minigame, null);
    }

    public Lobby(MinigameMatch match) {
        this(match.getMinigame(), match);
    }

    public Lobby() {
        this(null, null);
    }

    public boolean onJoin(CustomPlayer player) {
        PlayerUtils.changeWorld(player, spawnPos);

        player.currentMinigame = minigame;
        PlayerUtils.setLobbyState(player, onJoinState());

        onJoinBossBar(player);
        onJoinItems(player);

        player.setMatch(match);

        return true;
    }

    protected abstract PlayerStateType onJoinState();
    protected abstract void onJoinBossBar(CustomPlayer player);
    protected abstract void onJoinItems(CustomPlayer player);

    protected void createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<CustomPlayer> task) {
        return spawnNpc(configPath, task, false);
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<CustomPlayer> task, boolean subtitle) {
        configPath = YamlUtil.checkConfigPath(configPath);
        Position pos = YamlUtil.parsePosition(config.getString(configPath + "pos"), this.level);
        
        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));

        npc.updateTitle(config.getString(configPath + "text1"));
        if (subtitle) npc.updateSubtitle(config.getString(configPath + "text2"));

        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
        npc.setTask(task);
        npc.setSkin(config.getString(configPath + "skin-file"));

        npc.spawnToAll();

        return npc;
    }

    public void close() {
        WorldManager.unloadLevel(this.level);
    }

    public void initConfig() {
        this.config = getConfig();
        this.messages = getMessages();
    }

    public void reloadConfig() {
        this.spawnPos = YamlUtil.parsePosition(config.getString("lobby.spawn"), this.level);
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, boolean subtitle) {
        if (npc == null) return;

        configPath = YamlUtil.checkConfigPath(configPath);
        
        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
        npc.updateTitle(config.getString(configPath + "text1"));
        if (subtitle) {
            npc.updateSubtitle(config.getString(configPath + "text2"));
        }
    }

    public Level getLevel() { return this.level; }
    public abstract Config getConfig();
    public abstract Config getMessages();
    public abstract String requireConfigPath();
    public String configPath() { return YamlUtil.checkConfigPath(requireConfigPath()); }

}
