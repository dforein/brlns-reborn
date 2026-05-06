package com.brlnsreb.minigames.generallobby;

import java.util.List;

import com.brlnsreb.minigames.core.lobby.Lobby;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.minigame.MinigameType;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;

public class GeneralLobby extends Lobby {

    public GeneralLobby(Config config) {
        super(config);
        spawnAllNPCs();
    }

    private void spawnAllNPCs() {
        int X = 0;
        int Y = 1;
        int Z = 2;

        MinigameManager minigameManager = MinigameManager.getInstance();

        for (String gameNameTag : (List<String>) config.getList("npc.list")) {
            String path = "npc." + gameNameTag + ".";
            String rawCoords = config.getString(path + "pos");
            MinigameType minigame = MinigameType.fromNameTag(gameNameTag);

            spawnNPC(
                new Position(
                    getCoordinate(rawCoords, X) + 0.5,
                    getCoordinate(rawCoords, Y),
                    getCoordinate(rawCoords, Z) + 0.5,
                    this.level
                ),
                config.getString(path + "text1"),
                config.getString(path + "text2"),
                config.getDouble(path + "default-yaw"),
                (Player player) -> {
                    minigameManager.onJoin(player, minigame);
                },
                config.getString(path + "skin-file")
            );
        }
    }

    public boolean onJoin(Player player) {

    }

    private double getCoordinate(String rawCoords, int coord) {
        return Double.parseDouble(
            rawCoords.split("\\s+") [coord]
        );
    }
    
}
