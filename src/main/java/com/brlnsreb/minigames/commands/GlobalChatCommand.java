package com.brlnsreb.minigames.commands;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class GlobalChatCommand extends Command {

    private final MinigameCore plugin;
    
    public GlobalChatCommand(MinigameCore plugin) {
        super("globalchat");
        this.setDescription("Toggle global server chat or local level chat");
        this.setPermission("admin");

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (plugin.getGlobalChat()) {
            plugin.setGlobalChat(false);
            sender.sendMessage(TextFormat.RED + "Global Chat disabled");
        } else {
            plugin.setGlobalChat(true);
            sender.sendMessage(TextFormat.GREEN + "Global Chat enabled");
        }
        return true;
    }

}
