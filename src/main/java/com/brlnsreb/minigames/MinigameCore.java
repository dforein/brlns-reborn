package com.brlnsreb.minigames;

import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import com.brlnsreb.minigames.mm.entities.ThrownSwordEntity;
import com.brlnsreb.minigames.mm.listeners.*;

import java.util.ArrayList;

import com.brlnsreb.minigames.commands.GlobalChatCommand;
import com.brlnsreb.minigames.commands.MMCommand;
import com.brlnsreb.minigames.commands.MMOperatorCommand;
import com.brlnsreb.minigames.commands.PingCommand;
import com.brlnsreb.minigames.commands.ReloadConfigCommand;
import com.brlnsreb.minigames.commands.SetRulesCommand;
import com.brlnsreb.minigames.commands.ToggleSaveCommand;
import com.brlnsreb.minigames.core.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.core.minigame.MinigameType;
import com.brlnsreb.minigames.listeners.general.EntityChunkListener;
import com.brlnsreb.minigames.listeners.general.BlockUpdateListener;
import com.brlnsreb.minigames.listeners.general.ChatListener;
import com.brlnsreb.minigames.listeners.general.PlayerCreationListener;

public class MinigameCore extends PluginBase {
    
    private static MinigameCore instance;
    private MinigameManager minigameManager;

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
            this.getLogger().info("§aCustom entities registered successfully.");
            
        } catch (cn.nukkit.registry.RegisterException e) {
            this.getLogger().error("Error during entities registration: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public void onEnable() {

        String[] resources = {
            "database.yml",
            "general-lobby/config.yml"
        };

        for (String file : resources) { 
            saveResource(file, false); 
        }
        for (MinigameType minigame : MinigameType.values()) {
            saveResource(minigame.getNameTag() + "/config.yml", false);
            saveResource(minigame.getNameTag() + "/messages.yml", false);
        }


        getServer().setDefaultLevel(
            getServer().getLevelByName(
                getConfig().getString("lobby.spawn.world")
            )
        );

        getServer().getDefaultLevel().setSpawnLocation(new Vector3(
            getConfig().getDouble("lobby.spawn.x"),
            getConfig().getDouble("lobby.spawn.y"),
            getConfig().getDouble("lobby.spawn.z")
        ));

        getServer().getDefaultLevel().save();

        for (Level level : getServer().getLevels().values()) {
            level.setAutoSave(false);
        }
        

        this.minigameManager = new MinigameManager();

        registerCommands();
        registerListeners();

        
        this.getLogger().info(TextFormat.DARK_GREEN + "brlnsreb Minigames enabled!");
    }
    
    @Override
    public void onDisable() {
        minigameManager.forceStop();

        this.getLogger().info(TextFormat.DARK_RED + "brlnsreb Minigames disabled!");

        if (saveAtShutdown) return;
        for (Level level : new ArrayList<>(getServer().getLevels().values())) {
            if (!level.getName().equals(getServer().getDefaultLevel().getName())) {
                getServer().unloadLevel(level, true);
            }
        }
    }

    private void registerCommands() {
        SimpleCommandMap cm = getServer().getCommandMap();

        cm.register("ping", new PingCommand());
        cm.register("reloadconfig", new ReloadConfigCommand(this));
        cm.register("globalchat", new GlobalChatCommand(this));
        cm.register("togglesave", new ToggleSaveCommand(this));
        cm.register("setrules", new SetRulesCommand(this));

        cm.register("mm", new MMCommand(this, mmGame));
        cm.register("mmop", new MMOperatorCommand(this, mmGame));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvents(new PlayerCreationListener(), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new BlockUpdateListener(), this);
        pm.registerEvents(new EntityChunkListener(), this);

        pm.registerEvents(new MMPlayerInteractListener(mmGame), this);
        pm.registerEvents(new MMProjectileHitListener(mmGame), this);
        pm.registerEvents(new MMPlayerPickupListener(mmGame), this);
        pm.registerEvents(new MMPlayerJoinQuitListener(mmGame), this);
        pm.registerEvents(new MMPlayerAttackListener(mmGame), this);
        pm.registerEvents(new MMPlayerChatListener(mmGame), this);
        pm.registerEvents(new MMFormResponseListener(mmGame), this);
    }
    
    public static MinigameCore getInstance() {
        return instance;
    }

    public boolean getGlobalChat() {
        return globalChat;
    }

    public void setGlobalChat(boolean value) {
        globalChat = value;
    }

    public boolean getSave() {
        return saveAtShutdown;
    }

    public void setSave(boolean value) {
        saveAtShutdown = value;

        for (Level level : getServer().getLevels().values()) {
            level.setAutoSave(saveAtShutdown);
        }
    }

    public int getDebugVar() {
        return debugVar;
    }

    public void setDebugVar(int value) {
        debugVar = value;
    }
}