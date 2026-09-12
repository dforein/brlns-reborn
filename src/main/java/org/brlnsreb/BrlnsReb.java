package org.brlnsreb;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.command.Command;
import org.powernukkitx.event.HandlerList;
import org.powernukkitx.event.Listener;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.plugin.PluginLogger;
import org.powernukkitx.plugin.annotation.PluginMeta;
import org.powernukkitx.registry.RegisterException;
import org.powernukkitx.registry.Registries;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.brlnsreb.commands.BrlnsCommand;
import org.brlnsreb.commands.maintenance.ToggleSaveCommand;
import org.brlnsreb.commands.maintenance.minigames.MMMapsCommand;
import org.brlnsreb.commands.maintenance.physics.PhysicsCommand;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.data.database.DatabaseManager;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.listeners.general.PhysicsBlockUpdateListener;
import org.brlnsreb.listeners.maintenance.MaintenanceInteractListener;
import org.brlnsreb.listeners.maintenance.MaintenanceJoinListener;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.brlnsreb.utils.config.Configs;

@PluginMeta(
    name = "brlnsreb",
    version = "2.0.0",
    api = {"3.0.4"},
    authors = {"brlnsreb"},
    description = "BrokenLens Reborn plugin"
)

public class BrlnsReb extends PluginBase {

    private static final String[] RESOURCES = {
        "global/config.yml",
        "global/database.yml",
        "global/messages.yml",
        "main_hub/config.yml",
        "main_hub/messages.yml"
    };

    private static final List<Class<? extends Listener>> ALWAYS_ACTIVE_LISTENERS = List.of(
        PhysicsBlockUpdateListener.class
    );

    private static final List<Class<? extends Command>> MAINTENANCE_COMMANDS = List.of(
        PhysicsCommand.class,
        ToggleSaveCommand.class
    );

    private static List<Class<? extends Command>> ALL_LEVELS_MAINTENANCE_COMMANDS = List.of(
        MMMapsCommand.class
    );

    private static final List<Class<? extends Listener>> ALL_LEVELS_MAINTENANCE_LISTENERS = List.of(
        MaintenanceJoinListener.class,
        MaintenanceInteractListener.class
    );
    
    public static BrlnsReb instance;
    public static PluginLogger logger;
    private static Server server;

    private static DatabaseManager databaseManager;
    private static MinigameManager minigameManager;
    private static MainHub mainHub;

    private static boolean underMaintenance;
    private static boolean loadAllLevels;
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
        underMaintenance = Configs.getGlobalConfig().getBoolean("maintenance.server-under-maintenance");
        loadAllLevels = underMaintenance ? Configs.getGlobalConfig().getBoolean("maintenance.load-all-levels") : false;

        server = getServer();
        server.getSettings().levelSettings().loadAllLevels(false);
        server.setDifficulty(2);
        server.getSettings().save();

        LevelManager.init();

        if (underMaintenance) {     //TODO test
            server.getSettings().baseSettings().allowList(true);
            server.getSettings().baseSettings().allowListMessage("Server is under maintenance.");

            registerCommands(MAINTENANCE_COMMANDS);
        }

        if (underMaintenance && loadAllLevels) {
            LevelManager.loadAllLevelsUnderMaintenance();

            logger.info(TextFormat.DARK_GREEN + "All levels loaded!");
            logger.info(TextFormat.DARK_GREEN + "BrokenLens Reborn server under maintenance.");
            logger.info(TextFormat.GREEN + "Global chat enabled in loadAllLevels mode: players can chat on different worlds.");
            logger.info(TextFormat.GREEN + "Minigame map management commands available in loadAllLevels mode: run /<minigameTag> (e.g. /mm).");
            logger.info(TextFormat.RED + "Remember that autosave is disabled. To enable it, run /togglesave");
            logger.info(TextFormat.RED + "Remember that block and flow physics are disabled. To manage them, use /physics <check|edit>");

            getScheduler().scheduleTask(() -> {
                unregisterBrlnsCommands();
                HandlerList.unregisterAll(this);

                registerCommands(ALL_LEVELS_MAINTENANCE_COMMANDS);
                registerListenersEvents(ALL_LEVELS_MAINTENANCE_LISTENERS);
                registerListenersEvents(ALWAYS_ACTIVE_LISTENERS);
            });
            
            return;
        }

        registerListenersEvents(ALWAYS_ACTIVE_LISTENERS);

        databaseManager = new DatabaseManager();

        AuthSystem.init();
        PlayerDataManager.init();

        minigameManager = new MinigameManager();
        prepareMainHub();
        
        logger.info(TextFormat.DARK_GREEN + "BrokenLens Reborn server " + (!underMaintenance ? "online!" : "under maintenance."));
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

        server.getSettings().baseSettings().defaultLevelName(mainHub.getMap().level.getName());
        server.setDefaultLevel(mainHub.getMap().level);
        server.getDefaultLevel().setSpawnLocation(mainHub.getMap().spawn);

        server.getSettings().save();
        server.getDefaultLevel().save();
    }

    @Override
    public void beforeStop() {
        if (underMaintenance && loadAllLevels) return;

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


    private void registerCommands(List<Class<? extends Command>> commandClasses) {
        for (Class<? extends Command> clazz : commandClasses) {
            try {
                server.getCommandMap().register("bl", clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerListenersEvents(List<Class<? extends Listener>> listenerClasses) {
        for (Class<? extends Listener> clazz : listenerClasses) {
            try {
                server.getPluginManager().registerEvents(clazz.getDeclaredConstructor().newInstance(), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterBrlnsCommands() {
        ArrayList<String> unregistered = new ArrayList<>();
        for (Entry<String, Command> command : server.getCommandMap().getCommands().entrySet()) {
            if (command.getValue() instanceof BrlnsCommand) {
                unregistered.add(command.getKey());
            }
        }
        server.getCommandMap().unregister(unregistered.toArray(new String[unregistered.size()]));
    }


    public static boolean isUnderMaintenance() { return underMaintenance; }
    public static boolean isLoadAllLevelsEnabled() { return loadAllLevels; }

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