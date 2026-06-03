package com.brlnsreb.minigames;

import cn.nukkit.Server;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import com.brlnsreb.minigames.mm.entities.ThrownSwordEntity;

import java.util.ArrayList;

import com.brlnsreb.minigames.commands.GlobalChatCommand;
import com.brlnsreb.minigames.commands.PingCommand;
import com.brlnsreb.minigames.commands.ReloadConfigCommand;
import com.brlnsreb.minigames.commands.ToggleSaveCommand;
import com.brlnsreb.minigames.core.WorldManager;
import com.brlnsreb.minigames.core.auth.AuthSystem;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.minigame.MinigameType;
import com.brlnsreb.minigames.core.player.PlayerDataManager;
import com.brlnsreb.minigames.generallobby.GeneralLobby;
import com.brlnsreb.minigames.listeners.general.EntityChunkListener;
import com.brlnsreb.minigames.listeners.general.FormResponseListener;
import com.brlnsreb.minigames.listeners.general.BlockUpdateListener;
import com.brlnsreb.minigames.listeners.general.ChatListener;
import com.brlnsreb.minigames.listeners.general.PlayerCreationListener;
import com.brlnsreb.minigames.listeners.general.PlayerQuitListener;

public class MinigameCore extends PluginBase {
    
    private static MinigameCore instance;
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
        Config config = new Config(getDataFolder() + "general-lobby/config.yml", Config.YAML);
        String folderName = config.getString("lobby.spawn.world");

        server.loadLevel(folderName);

        Level generalLobbyLevel = server.getLevelByName(folderName);
        generalLobbyLevel.setAutoSave(false);

        server.setDefaultLevel(
            generalLobbyLevel
        );

        server.getDefaultLevel().setSpawnLocation(new Vector3(
            config.getDouble("lobby.spawn.x"),
            config.getDouble("lobby.spawn.y"),
            config.getDouble("lobby.spawn.z")
        ));

        server.getDefaultLevel().save();

        this.generalLobby = new GeneralLobby(
            this,
            config, 
            new Config(getDataFolder() + "general-lobby/messages.yml", Config.YAML)
        );
    }

    private void registerCommands() {
        SimpleCommandMap cm = server.getCommandMap();

        cm.register("ping", new PingCommand());
        cm.register("reloadconfig", new ReloadConfigCommand(this));
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
}