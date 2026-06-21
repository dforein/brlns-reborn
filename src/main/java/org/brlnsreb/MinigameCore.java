package org.brlnsreb;

import cn.nukkit.Server;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import org.brlnsreb.mm.entities.DeadBodyEntity;
import org.brlnsreb.mm.entities.ThrownSwordEntity;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import java.util.ArrayList;

import org.brlnsreb.commands.GlobalChatCommand;
import org.brlnsreb.commands.PingCommand;
import org.brlnsreb.commands.ReloadConfigCommand;
import org.brlnsreb.commands.ToggleSaveCommand;
import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.PlayerDataManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.listeners.general.EntityChunkListener;
import org.brlnsreb.listeners.general.FormResponseListener;
import org.brlnsreb.listeners.general.BlockUpdateListener;
import org.brlnsreb.listeners.general.ChatListener;
import org.brlnsreb.listeners.general.PlayerCreationListener;
import org.brlnsreb.listeners.general.PlayerQuitListener;

public class MinigameCore extends PluginBase {
    
    private static MinigameCore instance;
    private MinigameManager minigameManager;
    private GeneralLobby generalLobby;
    private Server server;

    private boolean globalChat = false;
    private boolean saveAtShutdown = false;
    private int debugVar = 0;
    
    @Override
    public void onLoad() {

        instance = this;
        this.getLogger().info(TextFormat.WHITE + "brlnsreb Minigames loading...");

        try {
            Registries.ENTITY.registerCustomEntity(this, DeadBodyEntity.class);
            Registries.ENTITY.registerCustomEntity(this, ThrownSwordEntity.class);
            Registries.ENTITY.registerCustomEntity(this, NPCEntity.class);
            Registries.ENTITY.rebuildTag();
            getLogger().info("§aCustom entities registered successfully.");
            
        } catch (cn.nukkit.registry.RegisterException e) {
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

        startDeadMenusCheckTask();

        registerCommands();
        registerListeners();

        
        getLogger().info(TextFormat.DARK_GREEN + "brlnsreb Minigames enabled!");
    }
    
    @Override
    public void onDisable() {
        MinigameManager.forceStop();

        getLogger().info(TextFormat.DARK_RED + "brlnsreb Minigames disabled!");

        if (saveAtShutdown) return;
        for (Level level : new ArrayList<>(server.getLevels().values())) {
            if (!level.getName().equals(server.getDefaultLevel().getName())) {
                server.unloadLevel(level, true);
            }
        }
    }

    private void saveAllResources() {
        String[] resources = {
            "global/database.yml",
            "global/messages.yml",
            "general-lobby/config.yml",
            "general-lobby/messages.yml"
        };

        for (String file : resources) { 
            saveResource(file, false); 
        }
        for (MinigameType minigame : MinigameType.values()) {
            saveResource(minigame.getNameTag() + "/config.yml", false);
            saveResource(minigame.getNameTag() + "/messages.yml", false);
        }
    }

    private void prepareGeneralLobby() {
        this.generalLobby = new GeneralLobby();
        Config config = generalLobby.getConfig();

        server.setDefaultLevel(
            server.getLevelByName(config.getString("lobby.world"))
        );

        server.getDefaultLevel().setSpawnLocation(
            YamlUtil.parseVector3Centered(config.getString("lobby.spawn-pos"))
        );

        server.getDefaultLevel().save();
    }

    private void startDeadMenusCheckTask() {
        server.getScheduler().scheduleDelayedRepeatingTask(this, 
            () -> { MenuAbstract.checkDeadForms(); }, 
            5 * 60 * 20, 
            5 * 60 * 20
        );
    }

    private void registerCommands() {
        SimpleCommandMap cm = server.getCommandMap();

        cm.register("ping", new PingCommand());
        cm.register("reloadconfig", new ReloadConfigCommand());
        cm.register("globalchat", new GlobalChatCommand(this));
        cm.register("togglesave", new ToggleSaveCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = server.getPluginManager();
        
        pm.registerEvents(new PlayerCreationListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new BlockUpdateListener(), this);
        pm.registerEvents(new EntityChunkListener(), this);
        pm.registerEvents(new FormResponseListener(), this);
    }
    
    public static MinigameCore getInstance() { return instance; }
    public boolean getGlobalChat() { return globalChat; }
    public void setGlobalChat(boolean value) { globalChat = value; }
    public boolean getSave() { return saveAtShutdown; }

    public void setSave(boolean value) {
        saveAtShutdown = value;

        for (Level level : server.getLevels().values()) {
            level.setAutoSave(saveAtShutdown);
        }
    }

    public int getDebugVar() { return debugVar; }
    public void setDebugVar(int value) { debugVar = value; }

    public GeneralLobby getGeneralLobby() { return generalLobby; }
    public MinigameManager getMinigameManager() { return minigameManager; }
}