package org.brlnsreb.commands.op;

import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "forcestart", 
    permission = "admin",
    description = "Force match start",
    commandMode = CommandMode.RAW
)

public class ForceStartCommand extends Command {

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (sender instanceof CustomPlayer player && player.matchCurrent != null) {
            switch (player.matchCurrent.state()) {
                case WAITING_LOBBY, LOBBY_COUNTDOWN -> player.matchCurrent.getWaitingLobby().forceStart();
                default -> {}
            }
        } else {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
        }
        
        return true;
    }
    
}
