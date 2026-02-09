package com.brlnsreb.minigames;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import com.brlnsreb.minigames.mm.entities.ThrownSwordEntity;
import com.brlnsreb.minigames.mm.listeners.*;
import com.brlnsreb.minigames.commands.MMCommand;
import com.brlnsreb.minigames.commands.PingCommand;
import com.brlnsreb.minigames.commands.ReloadConfigCommand;

public class MinigameCore extends PluginBase {
    
    private static MinigameCore instance;
    private MurderMysteryGame mmGame;
    
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
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        mmGame = new MurderMysteryGame(this);
        
        getServer().getCommandMap().register("mm", new MMCommand(this, mmGame));
        getServer().getCommandMap().register("ping", new PingCommand());
        getServer().getCommandMap().register("reloadconfig", new ReloadConfigCommand(this));
        
        getServer().getPluginManager().registerEvents(new MMPlayerInteractListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMProjectileHitListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMPlayerPickupListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMPlayerJoinQuitListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMPlayerDeathListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMPlayerChatListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMFormResponseListener(mmGame), this);
        getServer().getPluginManager().registerEvents(new MMPlayerInventoryListener(mmGame), this);

        
        this.getLogger().info(TextFormat.DARK_GREEN + "brlnsreb Minigames enabled!");
    }
    
    @Override
    public void onDisable() {
        if (mmGame != null) {
            mmGame.forceStop();
        }
        this.getLogger().info(TextFormat.DARK_RED + "brlnsreb Minigames disabled!");
    }
    
    public static MinigameCore getInstance() {
        return instance;
    }
    
    public MurderMysteryGame getMMGame() {
        return mmGame;
    }
}