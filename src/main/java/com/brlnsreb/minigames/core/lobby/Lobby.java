package com.brlnsreb.minigames.core.lobby;

import java.util.function.Consumer;

import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public abstract class Lobby {

    protected final Level level;
    protected Config config;

    public Lobby(Config lobbyConfig) {
        this.config = lobbyConfig;
        this.level = Server.getInstance().getLevelByName(lobbyConfig.getString("world"));
    }

    public abstract boolean onJoin(Player player);
    
    protected void spawnNPC(Position pos, String text1, String text2, double defaultYaw, Consumer<Player> task, String skinFileName) {
        NPCEntity npc = new NPCEntity(pos.getChunk(), Entity.getDefaultNBT(pos));

        npc.updateText(text1, text2);
        npc.setDefaultPose(defaultYaw);
        npc.setTask(task);
        npc.setSkin(skinFileName);

        npc.spawnToAll();
    }

    protected void createHologram(Position pos, String text) {
        FloatingTextParticle holo = new FloatingTextParticle(pos, TextFormat.colorize(text));
        this.level.addParticle(holo);
    }

}
