package com.brlnsreb.minigames;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.listeners.*;
import com.brlnsreb.minigames.commands.MMCommand;
import com.brlnsreb.minigames.commands.PingCommand;

public class MinigameCore extends PluginBase {
    
    private static MinigameCore instance;
    private MurderMysteryGame game;
    
    @Override
    public void onLoad() {
        instance = this;
        this.getLogger().info(TextFormat.WHITE + "Murder Mystery loading...");
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        game = new MurderMysteryGame(this);
        
        getServer().getCommandMap().register("mm", new MMCommand(this, game));
        getServer().getCommandMap().register("ping", new PingCommand());
        
        getServer().getPluginManager().registerEvents(new MMPlayerInteractListener(game), this);
        getServer().getPluginManager().registerEvents(new MMProjectileHitListener(game), this);
        getServer().getPluginManager().registerEvents(new MMPlayerPickupListener(game), this);
        getServer().getPluginManager().registerEvents(new MMPlayerQuitListener(game), this);
        getServer().getPluginManager().registerEvents(new MMPlayerDeathListener(game), this);
        getServer().getPluginManager().registerEvents(new MMPlayerChatListener(game), this);
        getServer().getPluginManager().registerEvents(new MMFormResponseListener(game), this);
        getServer().getPluginManager().registerEvents(new MMPlayerInventoryListener(game), this);
        
        this.getLogger().info(TextFormat.DARK_GREEN + "Murder Mystery enabled!");
    }
    
    @Override
    public void onDisable() {
        if (game != null) {
            game.forceStop();
        }
        this.getLogger().info(TextFormat.DARK_RED + "Murder Mystery disabled!");
    }
    
    public static MinigameCore getInstance() {
        return instance;
    }
    
    public MurderMysteryGame getGame() {
        return game;
    }
}