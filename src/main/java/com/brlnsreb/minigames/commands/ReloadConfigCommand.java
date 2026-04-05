package com.brlnsreb.minigames.commands;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class ReloadConfigCommand extends Command {

    private final MinigameCore plugin;
    
    public ReloadConfigCommand(MinigameCore plugin) {
        super("reloadconfig");
        this.setDescription("Reload config.yml");
        this.setPermission("admin");
        
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) return true;

        plugin.reloadConfig();
        sender.sendMessage(TextFormat.GREEN + "Config file reloaded!");
        return true;
    }
}