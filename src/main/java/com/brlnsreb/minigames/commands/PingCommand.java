package com.brlnsreb.minigames.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class PingCommand extends Command {
    
    public PingCommand() {
        super("ping", "Check your ping", "/ping <player>");
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length > 0) {
            Player target = player.getServer().getPlayer(args[0]);
            
            if (target == null) {
                player.sendMessage(TextFormat.RED + "Player not found!");
                return true;
            }
            
            int ping = target.getPing();
            player.sendMessage(TextFormat.GREEN + target.getName() + "'s ping: " + 
                TextFormat.YELLOW + ping + "ms " + getPingColor(ping));
            return true;
        }
        
        int ping = player.getPing();
        player.sendMessage(TextFormat.GREEN + "Your ping: " + 
            TextFormat.YELLOW + ping + "ms " + getPingColor(ping));
        
        return true;
    }
    
    private String getPingColor(int ping) {
        if (ping < 50) {
            return TextFormat.GREEN + "●";
        } else if (ping < 100) {
            return TextFormat.YELLOW + "●";
        } else if (ping < 200) {
            return TextFormat.GOLD + "●";
        } else {
            return TextFormat.RED + "●";
        }
    }
}