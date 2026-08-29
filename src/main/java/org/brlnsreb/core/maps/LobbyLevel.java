package org.brlnsreb.core.maps;

import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.config.YamlUtil;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Location;

public class LobbyLevel extends MapLevel {

    public Location spawn;
    
    public LobbyLevel(Lobby lobby, boolean matchRelated) {
        super(
            lobby.getConfig(), 
            lobby.configPath(), 
            matchRelated
        );
    }

    protected Level loadLevel(boolean copyWorld) {
        return LevelManager.loadLobbyLevel(config.getString(configPath + "world"), copyWorld);
    }

    protected void loadSpawns() {
        spawn = YamlUtil.parseLocationCentered(
            config.getString(configPath + "spawn-pos"), 
            level,
            config.getInt(configPath + "spawn-yaw")
        );
    }

    public void onConfigReload() {
        loadSpawns();
    }

    public Location getSpawnFor(CustomPlayer player) {
        return spawn;
    }

    public boolean arePhysicsEnabled() {
        return false;
    }

}
