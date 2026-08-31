package org.brlnsreb.commands.op;

import org.brlnsreb.BrlnsReb;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "reloaddatabase", 
    permission = "admin",
    description = "Reload the database by retrying with refreshed database.yml values",
    commandMode = CommandMode.RAW
)

public class ReloadDatabaseCommand extends Command {

     @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        BrlnsReb.getDatabaseManager().retryInit();
        return true;
    }

}
