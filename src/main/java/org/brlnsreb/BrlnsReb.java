package org.brlnsreb;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.plugin.PluginLogger;
import org.powernukkitx.plugin.annotation.PluginMeta;
import org.powernukkitx.registry.RegisterException;
import org.powernukkitx.registry.Registries;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.TextFormat;

import java.util.ArrayList;

import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.data.database.DatabaseManager;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.brlnsreb.utils.config.Configs;

@PluginMeta(
    name = "brlnsreb",
    version = "2.0.0",
    api = {"3.0.0"},
    authors = {"brlnsreb"},
    description = "BrokenLens Reborn plugin"
)

public class BrlnsReb extends PluginBase {
    
    public static BrlnsReb instance;
    public static PluginLogger logger;
    private static boolean underMaintenance;
    private static Server server;

    private static DatabaseManager databaseManager;
    private static MinigameManager minigameManager;
    private static MainHub mainHub;

    private final String[] RESOURCES = {
            "global/config.yml",
            "global/database.yml",
            "global/messages.yml",
            "main_hub/config.yml",
            "main_hub/messages.yml"
        };

    private static boolean globalChat = false;
    private static boolean saveAtShutdown = false;
    private static int debugVar = 0;
    
    @Override
    public void onLoad() {
        instance = this;
        logger = getLogger();
        logger.info(TextFormat.WHITE + "BrokenLens Reborn server loading...");

        try {
            Registries.ENTITY.registerCustomEntity(this, DeadBodyEntity.class);
            Registries.ENTITY.registerCustomEntity(this, ThrownSwordEntity.class);
            Registries.ENTITY.registerCustomEntity(this, NPCEntity.class);
            Registries.ENTITY.rebuildTag();
            
        } catch (RegisterException e) {
            logger.error("Error during entities registration: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void onEnable() {
        saveAllResources();
        underMaintenance = Configs.getGlobalConfig().getBoolean("server-under-maintenance");

        server = getServer();

        if (underMaintenance) {
            server.getSettings().baseSettings().allowList(true);
            server.getSettings().save();
            server.reloadWhitelist();
        } else {
            server.getSettings().baseSettings().allowList(false);
            server.getSettings().save();
            server.reloadWhitelist();
        }

        server.setDifficulty(2);

        databaseManager = new DatabaseManager();

        LevelManager.init();
        AuthSystem.init();
        PlayerDataManager.init();

        minigameManager = new MinigameManager();
        prepareMainHub();
        
        logger.info(TextFormat.DARK_GREEN + "BrokenLens Reborn server enabled!");
    }

    @Override
    public void beforeStop() {
        MinigameManager.forceStop();

        for (Player p : server.getOnlinePlayers().values()) {
            if (!p.isOnline()) continue;
            PlayerDataManager.savePlayerDataSync(p.getUniqueId());
            p.save();
            p.kick("Server is shutting down: autokicking all players.");
        }
    }
    
    @Override
    public void onDisable() {
        logger.info(TextFormat.DARK_RED + "BrokenLens Reborn server disabled!");

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
            saveResource(mgt.nameTag + "/maps.yml", false);
        }
    }

    private void prepareMainHub() {
        mainHub = new MainHub();

        server.setDefaultLevel(mainHub.getMap().level);
        server.getDefaultLevel().setSpawnLocation(mainHub.getMap().spawn);

        server.getDefaultLevel().save();
    }

    public static boolean isUnderMaintenance() { return underMaintenance; }

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

    public static DatabaseManager getDatabaseManager() { return databaseManager; }
    public static MinigameManager getMinigameManager() { return minigameManager; }
    public static ServerScheduler getScheduler() { return server.getScheduler(); }
}