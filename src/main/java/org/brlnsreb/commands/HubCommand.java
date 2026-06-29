package org.brlnsreb.commands;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.annotation.CommandDefinition;
import cn.nukkit.utils.TextFormat;

@CommandDefinition(
    name = "hub",
    aliases = {"lobby"},
    description = "Go to lobby"
)

public class HubCommand extends cn.nukkit.command.Command {
    
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
                MinigameMatch match = player.getMatch();
                if (match != null) match.onLeave(player);
                player.currentMinigame.onLobbyJoin(player);
                break;
        }
       //TODO: hub/lobby command (done?)
        
        return true;
    }
}
