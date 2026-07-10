package org.brlnsreb.commands;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "hub",
    aliases = {"lobby"},
    description = "Go to lobby"
)

public class HubCommand extends org.powernukkitx.command.Command {
    
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
