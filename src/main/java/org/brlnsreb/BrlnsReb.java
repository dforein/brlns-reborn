package org.brlnsreb;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.registry.RegisterException;
import org.powernukkitx.registry.Registries;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

import org.brlnsreb.utils.YamlUtil;

import java.util.ArrayList;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;

public class BrlnsReb extends PluginBase {
    
    public static BrlnsReb instance;
    private static Server server;

    private MinigameManager minigameManager;
    private GeneralLobby generalLobby;

    private final String[] RESOURCES = {
            "global/config.yml",
            "global/database.yml",
            "global/messages.yml",
            "general-lobby/config.yml",
            "general-lobby/messages.yml"
        };

    private static boolean globalChat = false;
    private static boolean saveAtShutdown = false;
    private static int debugVar = 0;
    
    @Override
    public void onLoad() {

        instance = this;
        this.getLogger().info(TextFormat.WHITE + "BrokenLens Reborn server loading...");

        try {
            Registries.ENTITY.registerCustomEntity(this, DeadBodyEntity.class);
            Registries.ENTITY.registerCustomEntity(this, ThrownSwordEntity.class);
            Registries.ENTITY.registerCustomEntity(this, NPCEntity.class);
            Registries.ENTITY.rebuildTag();
            
        } catch (RegisterException e) {
            getLogger().error("Error during entities registration: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public void onEnable() {
        server = getServer();

        saveAllResources();

        WorldManager.init();
        AuthSystem.init();
        PlayerDataManager.init();

        prepareGeneralLobby();
        minigameManager = new MinigameManager();

        
        this.getLogger().info(TextFormat.DARK_GREEN + "BrokenLens Reborn server enabled!");
    }

    @Override
    public void beforeStop() {
        MinigameManager.forceStop(true);

        for (Player p : server.getOnlinePlayers().values()) {
            if (!p.isOnline()) continue;
            PlayerDataManager.savePlayerDataSync(p.getUniqueId());
            p.save();
            p.kick("Server is shutting down");
        }
    }
    
    @Override
    public void onDisable() {
        this.getLogger().info(TextFormat.DARK_RED + "BrokenLens Reborn server disabled!");

        if (saveAtShutdown) return;
        for (Level level : new ArrayList<>(server.getLevels().values())) {
            if (level.getId() == server.getDefaultLevel().getId()) continue;
            server.unloadLevel(level, true);
        }
    }

    private void saveAllResources() {
        for (String file : RESOURCES) { 
            saveResource(file, false); 
        }
        for (MinigameType mgt : MinigameType.values()) {
            saveResource(mgt.nameTag + "/config.yml", false);
            saveResource(mgt.nameTag + "/messages.yml", false);
        }
    }

    private void prepareGeneralLobby() {
        this.generalLobby = new GeneralLobby();
        Config config = generalLobby.getConfig();

        server.setDefaultLevel(
            server.getLevelByName(config.getString("world"))
        );

        server.getDefaultLevel().setSpawnLocation(
            YamlUtil.parseVector3Centered(config.getString("spawn-pos"))
        );

        server.getDefaultLevel().save();
    }

    public static boolean getGlobalChat() { return globalChat; }
    public static void setGlobalChat(boolean value) { globalChat = value; }
    public static boolean getSave() { return saveAtShutdown; }

    public static void setSave(boolean value) {
        saveAtShutdown = value;

        for (Level level : server.getLevels().values()) {
            level.setAutoSave(saveAtShutdown);
        }
    }

    public static int getDebugVar() { return debugVar; }
    public static void setDebugVar(int value) { debugVar = value; }

    public GeneralLobby getGeneralLobby() { return generalLobby; }
    public MinigameManager getMinigameManager() { return minigameManager; }
}