package com.brlnsreb.minigames.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class HubCommand extends Command {
    
    public HubCommand() {
        super("hub");
        this.setDescription("Go to lobby");
        this.setAliases(new String[] {
            "hub",
            "lobby"
        });
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
       //TODO: hub/lobby command
        
        return true;
    }
}
