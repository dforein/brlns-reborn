package com.brlnsreb.minigames.core.lobby;

import java.util.function.Consumer;

import com.brlnsreb.minigames.core.lobby.entities.HologramEntity;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;

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
        this.spawnPos = parsePosition(config.getString("lobby.spawn"), this.level);
    }

    public abstract boolean onJoin(Player player);

    public void reloadConfig(Config config, Config messages) {
        this.config = config;
        this.messages = messages;
    }

    protected void createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
    }
    
    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task, boolean subtitle) {
        configPath += ".";

        Position pos = parsePosition(config.getString(configPath + "pos"), this.level);

        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        npc.updateTitle(config.getString(configPath + "text1"));
        if (subtitle) { npc.updateSubTitle(config.getString(configPath + "text2")); }
        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
        npc.setTask(task);
        npc.setSkin(config.getString(configPath + "skin-file"));

        npc.spawnToAll();

        return npc;
    }

    protected void reloadNpcConfigData(NPCEntity npc, String configPath, boolean subtitle) {
        npc.updateTitle(config.getString(configPath + "text1"));
        if (subtitle) { npc.updateSubTitle(config.getString(configPath + "text2")); }
        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
    }

    private Position parsePosition(String rawCoords, Level level) {
        int X = 0;
        int Y = 1;
        int Z = 2;

        return new Position(
            parseCoordinate(rawCoords, X) + 0.5,
            parseCoordinate(rawCoords, Y),
            parseCoordinate(rawCoords, Z) + 0.5,
            level
        );
    }

    private double parseCoordinate(String rawCoords, int coord) {
        return Double.parseDouble(
            rawCoords.split("\\s+") [coord]
        );
    }

    public Level getLevel() { return this.level; }
    public Config getConfig() { return this.config; }
    public Config getMessages() { return this.messages; }

}
