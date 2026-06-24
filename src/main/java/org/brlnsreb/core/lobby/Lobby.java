package org.brlnsreb.core.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.lobby.entities.HologramEntity;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

public abstract class Lobby {

    protected final Level level;
    protected final Minigame minigame;
    protected Position spawnPos;
    protected Config config;
    protected Config messages;
    protected Map<NPCEntity, String> npcConfigPathMap = new HashMap<>();

    public Lobby(Minigame minigame) {
        this.minigame = minigame;
        this.config = getConfig();
        this.messages = getMessages();

        String levelPath = configPath().equals("") ?
            "world" : configPath() + ".world";
        this.level = WorldManager.loadLobbyLevel(config.getString(levelPath));
        this.spawnPos = YamlUtil.parsePosition(config.getString(configPath() + "spawn"), this.level);
    }

    public Lobby() {
        this(null);
    }

    public boolean onJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;

        p.currentMinigame = minigame;
        PlayerUtils.changeWorld(p, spawnPos);
        PlayerUtils.setLobbyState(p, onJoinState());

        onJoinBossBar(p);
        onJoinItems(p);

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

    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task) {
        return spawnNpc(configPath, task, false);
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task, boolean subtitle) {
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
    public abstract String getConfigPath();
    public String configPath() { return YamlUtil.checkConfigPath(getConfigPath()); }

}
