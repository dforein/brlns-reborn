package org.brlnsreb.commands;

import org.brlnsreb.core.player.CustomPlayer;

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
        
        CustomPlayer player = (CustomPlayer) sender;
        
        switch (player.state) {
            case LOBBY:
                player.sendMessage("");
                break;

            default:
                player.getMatch().onLeave(player);
                player.currentMinigame.onLobbyJoin(player);
                break;
        }
       //TODO: hub/lobby command (done?)
        
        return true;
    }
}
