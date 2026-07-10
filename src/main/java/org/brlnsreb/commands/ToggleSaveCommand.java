package org.brlnsreb.commands;

import org.brlnsreb.BrlnsReb;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "togglesave", 
    permission = "admin",
    description = "Toggle saving worlds at server shutdown"
)

public class ToggleSaveCommand extends cn.nukkit.command.Command {

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (BrlnsReb.getSave()) {
            BrlnsReb.setSave(false);
            sender.sendMessage(TextFormat.RED + "Saving disabled");
        } else {
            BrlnsReb.setSave(true);
            sender.sendMessage(TextFormat.GREEN + "Saving enabled");
        }
        return true;
    }

}
