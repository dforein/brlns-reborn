package com.brlnsreb.minigames.core.lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.WorldManager;
import com.brlnsreb.minigames.core.lobby.entities.HologramEntity;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.core.player.PlayerUtils;
import com.brlnsreb.minigames.utils.YAMLUtil;

import cn.nukkit.Player;
import cn.nukkit.Server;
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
        this.config = getNewConfig();
        this.messages = getNewMessages();

        String levelPath = configPath().equals("") ?
            "world" : configPath() + ".world";
        this.level = WorldManager.loadLobbyLevel(config.getString(levelPath));
        this.spawnPos = YAMLUtil.parsePosition(config.getString(configPath() + "spawn"), this.level);
    }

    public Lobby() {
        this.minigame = null;
        this.config = getNewConfig();
        this.messages = getNewMessages();

        String levelPath = configPath().equals("") ?
            "world" : configPath() + ".world";
        this.level = WorldManager.loadLobbyLevel(config.getString(levelPath));
        this.spawnPos = YAMLUtil.parsePosition(config.getString(configPath() + "spawn"), this.level);
    }

    public boolean onJoin(Player player) {
        CustomPlayer p = (CustomPlayer) player;

        p.currentMinigame = minigame;
        PlayerUtils.changeWorld(p, spawnPos);
        PlayerUtils.setLobbyState(p);

        onJoinBossBar(p);
        onJoinItems(p);

        return true;
    }

    protected abstract void onJoinBossBar(CustomPlayer player);
    protected abstract void onJoinItems(CustomPlayer player);

    protected void createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task) {
        configPath = YAMLUtil.checkConfigPath(configPath);

        Position pos = YAMLUtil.parsePosition(config.getString(configPath + "pos"), this.level);

        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        npc.updateTitle(config.getString(configPath + "text1"));
        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
        npc.setTask(task);
        npc.setSkin(config.getString(configPath + "skin-file"));

        npc.spawnToAll();

        return npc;
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task, boolean subtitle) {
        configPath = YAMLUtil.checkConfigPath(configPath);

        NPCEntity npc = spawnNpc(configPath, task);
        npc.updateSubtitle(config.getString(configPath + "text2"));
        return npc;
    }
    
    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task, boolean subtitle, Minigame minigameForPlayerCount) {
        configPath = YAMLUtil.checkConfigPath(configPath);

        NPCEntity npc = spawnNpc(configPath, task);

        npc.setPlayerCountLine(config.getString(configPath + "text2"));
        Server.getInstance().getScheduler().scheduleRepeatingTask(MinigameCore.getInstance(), 
            () -> {
                npc.updatePlayerCountSubtitle(minigameForPlayerCount.getPlayerCount());
            }, 100
        );

        return npc;
    }

    public void close() {
        WorldManager.unloadLevel(this.level);
    }

    public void initConfig() {
        this.config = getNewConfig();
        this.messages = getNewMessages();
    }

    public void reloadConfig(boolean reloadConfig) {
        if (reloadConfig) {
            this.config = getNewConfig();
            this.messages = getNewMessages();
        }
        this.spawnPos = YAMLUtil.parsePosition(config.getString("lobby.spawn"), this.level);
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, boolean subtitle, boolean playerCountSubtitle) {
        if (npc == null) return;

        configPath = YAMLUtil.checkConfigPath(configPath);
        
        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
        npc.updateTitle(config.getString(configPath + "text1"));
        if (subtitle) {
            if (playerCountSubtitle) {
                npc.setPlayerCountLine(config.getString(configPath + "text2"));
            } else {
                npc.updateSubtitle(config.getString(configPath + "text2"));
            }
        }
    }

    public Level getLevel() { return this.level; }
    public Config getConfig() { return this.config; }
    public Config getMessages() { return this.messages; }
    public abstract Config getNewConfig();
    public abstract Config getNewMessages();
    public abstract String getConfigPath();
    public String configPath() { return YAMLUtil.checkConfigPath(getConfigPath()); }

}
