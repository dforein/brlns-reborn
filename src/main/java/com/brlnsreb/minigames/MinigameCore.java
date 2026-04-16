package com.brlnsreb.minigames;

import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.level.Level;
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
import com.brlnsreb.minigames.listeners.BlockUpdateListener;
import com.brlnsreb.minigames.listeners.ChatListener;

public class MinigameCore extends PluginBase {
    
    private static MinigameCore instance;
    private MurderMysteryGame mmGame;
    private boolean globalChat;
    private boolean saveAtShutdown;
    private int debugVar;
    
    @Override
    public void onLoad() {
        instance = this;
        this.getLogger().info(TextFormat.WHITE + "brlnsreb Minigames loading...");

        try {
            Registries.ENTITY.registerCustomEntity(this, DeadBodyEntity.class);
            Registries.ENTITY.registerCustomEntity(this, ThrownSwordEntity.class);
            Registries.ENTITY.rebuildTag();
            this.getLogger().info("§aCustom entities registered successfully.");
            
        } catch (cn.nukkit.registry.RegisterException e) {
            this.getLogger().error("Error during DeadBodyEntity registration: " + e.getMessage());
            throw new RuntimeException(e);
        }

        debugVar = 0;
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        globalChat = false;
        saveAtShutdown = false;
        mmGame = new MurderMysteryGame(this);


        SimpleCommandMap cm = getServer().getCommandMap();
        PluginManager pm = getServer().getPluginManager();
        
        cm.register("mm", new MMCommand(this, mmGame));
        cm.register("mmop", new MMOperatorCommand(this, mmGame));
        cm.register("ping", new PingCommand());
        cm.register("reloadconfig", new ReloadConfigCommand(this));
        cm.register("globalchat", new GlobalChatCommand(this));
        cm.register("togglesave", new ToggleSaveCommand(this));
        cm.register("setrules", new SetRulesCommand(this));
        
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new BlockUpdateListener(), this);

        pm.registerEvents(new MMPlayerInteractListener(mmGame), this);
        pm.registerEvents(new MMProjectileHitListener(mmGame), this);
        pm.registerEvents(new MMPlayerPickupListener(mmGame), this);
        pm.registerEvents(new MMPlayerJoinQuitListener(mmGame), this);
        pm.registerEvents(new MMPlayerAttackListener(mmGame), this);
        pm.registerEvents(new MMPlayerChatListener(mmGame), this);
        pm.registerEvents(new MMFormResponseListener(mmGame), this);
        pm.registerEvents(new MMPlayerInventoryListener(mmGame), this);


        for (Level level : getServer().getLevels().values()) {
            level.setAutoSave(false);
        }

        
        this.getLogger().info(TextFormat.DARK_GREEN + "brlnsreb Minigames enabled!");
    }
    
    @Override
    public void onDisable() {
        if (mmGame != null) {
            mmGame.forceStop();
        }

        this.getLogger().info(TextFormat.DARK_RED + "brlnsreb Minigames disabled!");

        if (saveAtShutdown) return;
        for (Level level : new ArrayList<>(getServer().getLevels().values())) {
            if (!level.getName().equals(getServer().getDefaultLevel().getName())) {
                getServer().unloadLevel(level, true);
            }
        }
    }
    
    public static MinigameCore getInstance() {
        return instance;
    }
    
    public MurderMysteryGame getMMGame() {
        return mmGame;
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