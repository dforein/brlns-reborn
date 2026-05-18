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
    protected Config config;

    public Lobby(Config lobbyConfig) {
        this.config = lobbyConfig;
        this.level = Server.getInstance().getLevelByName(lobbyConfig.getString("world"));
    }

    public abstract boolean onJoin(Player player);

    protected void createHologram(Position pos, String text) {
        HologramEntity holo = new HologramEntity(pos.getChunk(), Entity.getDefaultNBT(pos));
        holo.setText(text);
        holo.spawnToAll();
    }
    
    protected NPCEntity spawnNpc(String configPath, Consumer<Player> task) {
        //remember to put the subtitle later

        configPath += ".";

        int X = 0;
        int Y = 1;
        int Z = 2;
        String rawCoords = config.getString(configPath + "pos");
        Position pos = new Position(
            parseCoordinate(rawCoords, X) + 0.5,
            parseCoordinate(rawCoords, Y),
            parseCoordinate(rawCoords, Z) + 0.5,
            this.level
        );

        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));

        npc.updateTitle(
            config.getString(configPath + "text1")
        );
        npc.setDefaultPose(config.getDouble(configPath + "default-yaw"));
        npc.setTask(task);
        npc.setSkin(config.getString(configPath + "skin-file"));

        npc.spawnToAll();

        return npc;
    }

    private double parseCoordinate(String rawCoords, int coord) {
        return Double.parseDouble(
            rawCoords.split("\\s+") [coord]
        );
    }

}
