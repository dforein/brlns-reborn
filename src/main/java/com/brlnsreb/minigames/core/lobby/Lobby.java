package com.brlnsreb.minigames.core.lobby;

import java.util.function.Consumer;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.lobby.entities.HologramEntity;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.Minigame;
import com.brlnsreb.minigames.utils.YAMLUtil;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

public abstract class Lobby {

    protected final Level level;
    protected Position spawnPos;
    protected Config config;
    protected Config messages;

    public Lobby(Config config, Config messages) {
        this.config = config;
        this.messages = messages;
        this.level = Server.getInstance().getLevelByName(config.getString("lobby.world"));
        this.spawnPos = YAMLUtil.parsePosition(config.getString("lobby.spawn"), this.level);
    }

    public abstract boolean onJoin(Player player);

    protected void createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
    }

    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task) {
        configPath += ".";

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
        NPCEntity npc = spawnNpc(configPath, task);
        npc.updateSubtitle(config.getString(configPath + "text2")); 

        return npc;
    }
    
    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task, boolean subtitle, Minigame minigameForPlayerCount) {
        NPCEntity npc = spawnNpc(configPath, task);

        npc.setPlayerCountLine(config.getString(configPath + "text2"));
        Server.getInstance().getScheduler().scheduleRepeatingTask(MinigameCore.getInstance(), 
            () -> {
                npc.updatePlayerCountSubtitle(minigameForPlayerCount.getPlayerCount());
            }, 100
        );

        return npc;
    }

    public void reloadConfig(Config config, Config messages) {
        this.config = config;
        this.messages = messages;
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, boolean subtitle, boolean playerCountSubtitle) {
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

}
