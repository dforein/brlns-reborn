package org.brlnsreb.commands;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.mainhub.MainHub;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "hub",
    aliases = {"lobby"},
    description = "Go to lobby",
    commandMode = CommandMode.RAW
)

public class HubCommand extends Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }
        
        CustomPlayer player = (CustomPlayer) sender;
        
        switch (player.state) {
            case LOBBY:
                MainHub.instance.onJoin(player);
                break;

            default:
                Match match = player.matchCurrent;
                if (match != null) match.onLeave(player);
                player.minigameCurrent.onLobbyJoin(player);
                break;
        }
        
        return true;
    }
}
